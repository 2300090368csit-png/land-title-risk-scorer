<h1 align="center">🏡 Land Title Risk Scorer</h1>

<p align="center">
A full-stack Java app that looks at a piece of land in Andhra Pradesh and gives it<br/>
a 0–100 "title risk score" — like a credit score, but for whether the paperwork is clean.
</p>

<p align="center">
<b>Backend:</b> Java 17 + Spring Boot 3 &nbsp;|&nbsp;
<b>Frontend:</b> plain HTML/CSS/JavaScript &nbsp;|&nbsp;
<b>Database:</b> H2 (in-memory, zero setup)
</p>

---

## Table of contents

- [What is this, really?](#what-is-this-really)
- [The 5 things it checks (in plain English)](#the-5-things-it-checks-in-plain-english)
- [How it works, step by step](#how-it-works-step-by-step)
- [A worked example](#a-worked-example)
- [Project structure, explained](#project-structure-explained)
- [The design pattern behind the scoring](#the-design-pattern-behind-the-scoring)
- [Why rules instead of AI/ML](#why-rules-instead-of-aiml)
- [API reference](#api-reference)
- [Running it yourself](#running-it-yourself)
- [Tech stack](#tech-stack)

---

## What is this, really?

Before anyone buys a plot of land in India, a lawyer usually digs through five or
six government records to make sure the seller actually has the legal right to
sell it, and that nothing nasty is hiding in the paperwork (an old unpaid loan
against the land, a court case, etc.).

This app automates the *first pass* of that check. You feed it a parcel of land
(who's selling it, where it is, and the outcome of 5 standard checks), and it
gives back:

- **A single score from 0 to 100** — higher is safer, exactly like a credit score.
- **A plain-English reason for every point lost**, so the score isn't a black box.

It comes pre-loaded with 20 realistic sample parcels from around Andhra
Pradesh, so you can open it and start clicking around immediately — nothing to
configure, no database to install. There's also an **"Add a property"** page
where you can enter your own combination of the 5 checks and get a real,
live-computed score back — it runs through the exact same backend logic as
every seeded property, nothing about it is pre-canned.

> **Note on the "20 sample parcels":** these are fictional, made-up examples
> (survey numbers, names, and locations) written to demonstrate how the
> scoring works. They are not real property records.

## The 5 things it checks (in plain English)

Each of these is a real category of due diligence used when buying land in
India. If any of these terms are new to you, here's the plain-language version:

| # | Factor | Weight | What it actually means |
|---|---|---|---|
| 1 | **Encumbrance Certificate (EC)** | 30% | A government-issued document listing every loan, mortgage, or legal claim ever registered against this exact piece of land. If it's "flagged," there's an old unresolved claim still attached to the land — and that claim follows the *land*, not the seller, so a buyer inherits it. |
| 2 | **Litigation status** | 25% | Is anyone currently suing over this land, or has a court case been filed? If yes, the property is called **"sub judice"** — a court could freeze the sale or even undo it later, no matter how clean everything else looks. |
| 3 | **Layout approval** | 20% | If the land is part of a residential layout (plots, roads, etc.), has the relevant government planning authority actually approved that layout? Andhra Pradesh doesn't have one single authority for this — it's **CRDA** near the new capital (Amaravati), **VMRDA** around Visakhapatnam, or **DTCP** elsewhere. An unapproved layout can block future construction permits. |
| 4 | **AP RERA registration** | 15% | RERA is an Indian law that requires real estate *projects being sold to the public* to register with the state regulator. It gives buyers legal recourse if the builder doesn't deliver. A plain resale of agricultural land doesn't need this — only actively marketed projects do. |
| 5 | **MeeBhoomi digital record match** | 10% | MeeBhoomi is Andhra Pradesh's official website where land records are stored digitally. This check asks: does the paper deed match what the government's own database says? A mismatch is usually a data-entry slip, not fraud, but it has to be fixed before registration. |

The weights (30/25/20/15/10) add up to 100% and reflect how much legal weight
each check actually carries in practice — the EC and litigation status matter
far more than a website data-entry mismatch.

## How it works, step by step

Nothing here is magic — here's the exact sequence of events from the moment
you open the page to seeing a score on screen:

```mermaid
sequenceDiagram
    participant You as You (browser)
    participant FE as Frontend (index.html + JS)
    participant API as Backend (Spring Boot REST API)
    participant Logic as Scoring logic (5 factor classes)
    participant DB as H2 Database

    You->>FE: Open http://localhost:8080
    FE->>API: fetch("/api/parcels")
    API->>DB: "give me every parcel"
    DB-->>API: 20 rows of raw parcel data
    API->>Logic: score each parcel
    Logic-->>API: score + reasons for each
    API-->>FE: JSON response
    FE-->>You: renders the table you see
```

Click into any row and the exact same thing happens again, just for one
parcel, with the full breakdown of all 5 factors instead of just the total.

The important part: **the backend and frontend only ever talk in JSON.** The
backend has no idea what the page looks like — it just answers questions like
"what's parcel #7's score?" The frontend is just JavaScript asking those
questions and drawing the answer on screen. This is exactly how a mobile app
would talk to the same backend, too.

## A worked example

Let's score one real parcel from the seeded data by hand, so you can see
there's no hidden logic — it's just multiplication and addition.

**Parcel `19/2` — Machilipatnam, Krishna:**

| Factor | Status | Raw score (0–100) | Weight | Contribution |
|---|---|---|---|---|
| Encumbrance Certificate | Flagged | 15 | × 0.30 | 4.5 |
| Litigation | Active court case | 5 | × 0.25 | 1.25 |
| Layout approval | Unapproved | 10 | × 0.20 | 2.0 |
| AP RERA | Not registered | 30 | × 0.15 | 4.5 |
| MeeBhoomi match | Mismatch | 25 | × 0.10 | 2.5 |
| | | | **Total** | **14.75 → rounds to 15** |

That "15" is exactly what you'll see on the list page for that parcel — red,
because it's under 40. Open its detail page in the running app and you'll see
these same 5 numbers, plus the plain-English reason behind each one.

## Project structure, explained

If you've never opened a Spring Boot project before, here's what each folder
is actually for, in order of "how a request flows through them":

```
land-title-risk-scorer/
├── src/main/java/com/titlerisk/
│   │
│   ├── model/            The "nouns" of the app. Parcel is a plot of land;
│   │                     the 5 enums (EcStatus, LitigationStatus, etc.) are
│   │                     just fixed lists of allowed answers, e.g. an EC can
│   │                     only ever be CLEAN or FLAGGED — nothing else.
│   │
│   ├── repository/        One interface, ParcelRepository. This is what
│   │                     talks to the database. You never write SQL here —
│   │                     Spring Data generates it for you.
│   │
│   ├── service/           The "brain." RiskScoringService takes a Parcel and
│   │   └── factors/       produces a score. The factors/ subfolder has one
│   │                     small class per due-diligence check (see below).
│   │
│   ├── dto/                Shapes the backend's JSON requests/responses. Keeps
│   │                     the database structure (Parcel) separate from what
│   │                     the API actually sends/accepts over the wire —
│   │                     includes CreateParcelRequest for the write path.
│   │
│   ├── controller/         ParcelApiController — the only class that knows
│   │                     about HTTP. Exposes GET and POST /api/parcels.
│   │
│   └── config/              DataSeeder — runs once on startup and inserts the
│                          20 sample parcels so there's something to look at.
│
├── src/main/resources/
│   ├── application.properties   App settings (which port, database URL, etc.)
│   └── static/                  The frontend — plain HTML/CSS/JS, no
│       ├── index.html           build step. Served as-is by Spring Boot.
│       ├── parcel.html          Detail page for one property.
│       ├── add.html             "Add a property" form — try your own data.
│       ├── css/style.css
│       └── js/                  common.js (shared helpers + factor glossary),
│                                 list.js, detail.js, add.js — one per page.
│
├── pom.xml               Tells Maven which libraries (Spring Boot, H2, etc.)
│                        this project depends on, and how to build it.
├── mvnw / mvnw.cmd       Lets you build/run the project without installing
│                        Maven yourself (see "Running it yourself" below).
└── README.md              You are here.
```

## The design pattern behind the scoring

This is the part that's actually interesting from a software design point of
view, so it's worth walking through slowly.

**The naive way to write this** would be one big method with an if/else (or
switch) for every factor, something like:

```java
// what this project deliberately avoids:
double score = 0;
if (ec == CLEAN) score += 30; else score += 4.5;
if (litigation == NONE) score += 25; else if (litigation == PENDING) score += 12.5; ...
// ...and so on for all 5 factors, all tangled together in one method.
```

The problem: every time you add a 6th check (say, a boundary survey dispute),
you have to open this method and edit it, right next to four other checks
that have nothing to do with your change. It gets messy fast, and it's easy
to break an existing check by accident.

**What this project does instead:**

1. There's one shared contract, the `RiskFactor` interface:
   ```java
   public interface RiskFactor {
       double getWeight();
       FactorScore evaluate(Parcel parcel);
   }
   ```
2. Each check is its own small class that implements that contract —
   `EncumbranceFactor`, `LitigationFactor`, `LayoutApprovalFactor`,
   `ReraFactor`, `MeeBhoomiFactor`. Each one only knows about *its own* rule.
3. `RiskScoringService` never mentions any of those class names. It just asks
   Spring for "every class that implements `RiskFactor`," loops over whatever
   it gets, and adds up the weighted results:
   ```java
   public RiskScoringService(List<RiskFactor> riskFactors) {
       this.riskFactors = riskFactors; // Spring fills this in automatically
   }
   ```

The payoff: to add a 6th factor, you write **one new file** and mark it
`@Component`. Spring finds it automatically and `RiskScoringService` starts
including it in the total — without a single line of `RiskScoringService`
changing. In software design terms, this is the **Open/Closed Principle**:
the scoring engine is *open* to adding new checks, but *closed* to needing
modification when you do.

## Why rules instead of AI/ML

It would be possible to train a machine learning model to predict title risk
instead of hand-writing these rules. Two reasons that's the wrong tool here:

1. **There's no real training data.** Nobody has a spreadsheet of 10,000 real
   Andhra Pradesh parcels with a verified "this one turned out to be risky"
   label. Without real labeled outcomes, a model would just be fit to made-up
   numbers — which isn't actually more trustworthy than writing the rules
   directly, it just hides the guesswork behind a black box instead of
   stating it in the open.
2. **A due-diligence tool has to be explainable.** If a buyer's lawyer asks
   "why does this parcel score 24?", a rule-based answer — "the EC is
   flagged and there's an active lawsuit" — is something they can go verify
   against the actual government records. A machine learning model's
   internal weights can't be checked against a legal document the same way.

## API reference

The backend is a plain JSON REST API. You can call it directly with `curl`,
Postman, or from any other app — it doesn't know or care that the bundled
frontend exists.

<details>
<summary><code>GET /api/parcels</code> — list every parcel with its score</summary>

```bash
curl http://localhost:8080/api/parcels
```

```json
[
  {
    "id": 1,
    "surveyNo": "142/2A",
    "sellerName": "K. Venkata Ramana Reddy",
    "locationArea": "Tullur, Guntur",
    "score": 100,
    "riskBand": "low"
  },
  { "...": "19 more rows" }
]
```

`riskBand` is `"low"` (score > 70, shown green), `"medium"` (40–70, yellow),
or `"high"` (< 40, red) — matches the color-coding in the UI.
</details>

<details>
<summary><code>GET /api/parcels/{id}</code> — full breakdown for one parcel</summary>

```bash
curl http://localhost:8080/api/parcels/1
```

```json
{
  "id": 1,
  "surveyNo": "142/2A",
  "sellerName": "K. Venkata Ramana Reddy",
  "locationArea": "Tullur, Guntur",
  "ecStatus": "CLEAN",
  "litigationStatus": "NONE",
  "layoutApproval": "APPROVED",
  "reraStatus": "REGISTERED",
  "meeBhoomiMatch": "MATCHED",
  "score": 100,
  "riskBand": "low",
  "factors": [
    {
      "factorName": "Encumbrance Certificate",
      "rawScore": 100.0,
      "weightPercent": 30,
      "explanation": "Encumbrance Certificate shows no registered liens, mortgages, or unresolved entries for this survey number — the strongest available evidence of a clean chain of title."
    },
    { "...": "4 more factors" }
  ]
}
```

Returns `404 Not Found` if the id doesn't exist.
</details>

<details>
<summary><code>POST /api/parcels</code> — add a new property and get its score back</summary>

```bash
curl -X POST http://localhost:8080/api/parcels \
  -H "Content-Type: application/json" \
  -d '{
    "surveyNo": "1/A",
    "sellerName": "Jane Doe",
    "locationArea": "Example Village, Guntur",
    "ecStatus": "CLEAN",
    "litigationStatus": "NONE",
    "layoutApproval": "APPROVED",
    "reraStatus": "REGISTERED",
    "meeBhoomiMatch": "MATCHED"
  }'
```

Returns `201 Created` with the same shape as `GET /api/parcels/{id}` — the
newly assigned `id` and its full computed score, ready to redirect to.

All 8 fields are required (every one of them feeds directly into the score).
A missing or blank field returns `400 Bad Request` with a plain-English reason:

```json
{ "status": 400, "error": "Bad Request", "message": "Survey number is required." }
```

This is exactly what powers the **Add a property** page — it's a thin HTML
form over this same endpoint, so anything you can do through the UI you can
also do with a script.
</details>

## Running it yourself

You need **Java 17** installed. You do **not** need Maven installed — this
project bundles a Maven wrapper that downloads the right version for you
automatically the first time you run it.

**1. Clone the repository**

```bash
git clone https://github.com/sriram32086/land-title-risk-scorer.git
cd land-title-risk-scorer
```

**2. Run it**

On Windows:
```bash
mvnw.cmd spring-boot:run
```

On Mac/Linux:
```bash
./mvnw spring-boot:run
```

The first run will take a minute or two while it downloads Maven and all the
Java libraries — that's normal, and only happens once.

**3. Open it**

Once you see `Started TitleRiskScorerApplication` in the terminal, open:

👉 **http://localhost:8080**

To stop it, go back to the terminal and press `Ctrl+C`.

**Extra: peek at the raw database.** While the app is running, open
[http://localhost:8080/h2-console](http://localhost:8080/h2-console) — use
JDBC URL `jdbc:h2:mem:titleriskdb`, username `sa`, and leave the password
blank. This is an in-memory database, so every restart resets it back to the
same 20 sample parcels — there's nothing to lose or break.

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3 (Spring Web, Spring Data JPA) |
| Database | H2 (in-memory — no install, no setup) |
| Frontend | Plain HTML, CSS, and JavaScript (`fetch` API) — no framework, no build step |
| Build tool | Maven (wrapper included, so a local Maven install isn't required) |

---

<p align="center"><sub>Built as a portfolio project. Sample data is fictional.</sub></p>
