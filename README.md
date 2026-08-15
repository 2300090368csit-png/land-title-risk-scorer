<h1 align="center">🏡 Land Title Risk Scorer</h1>

<p align="center">
A full-stack Java app that looks at a piece of land in Andhra Pradesh and gives it<br/>
a 0–100 "title risk score" — like a credit score, but for whether the paperwork is clean.
</p>

<p align="center">
<img alt="Java 17" src="https://img.shields.io/badge/Java-17-b07219">
<img alt="Spring Boot 3" src="https://img.shields.io/badge/Spring%20Boot-3-6DB33F">
<img alt="Spring Security" src="https://img.shields.io/badge/Spring%20Security-session%20auth-6DB33F">
<img alt="Tests" src="https://img.shields.io/badge/tests-50%20passing-brightgreen">
<a href="LICENSE"><img alt="MIT License" src="https://img.shields.io/badge/license-MIT-blue"></a>
<a href="https://land-title-risk-scorer.onrender.com/"><img alt="Live demo" src="https://img.shields.io/badge/demo-online-success"></a>
</p>

<p align="center">
<b>Backend:</b> Java 17 + Spring Boot 3 &nbsp;|&nbsp;
<b>Frontend:</b> plain HTML/CSS/JavaScript &nbsp;|&nbsp;
<b>Database:</b> H2 (in-memory, zero setup)
</p>

<p align="center">
<a href="https://land-title-risk-scorer.onrender.com/"><b>▶ Try the live demo</b></a><br/>
<sub>Sign in with <code>demo</code> / <code>demo1234</code> — or create your own account.<br/>
Hosted on a free instance, so if it has been idle a while the first page can take ~50 seconds to wake up.</sub>
</p>

---

## Table of contents

1. [What is this, really?](#1-what-is-this-really)
2. [Running it yourself](#2-running-it-yourself)
&nbsp;&nbsp;&nbsp;2b. [Deploying it so other people can open a link](#2b-deploying-it-so-other-people-can-open-a-link)
3. [Screens and how you move between them](#3-screens-and-how-you-move-between-them)
4. [The 9 things it checks (in plain English)](#4-the-9-things-it-checks-in-plain-english)
5. [How it works, step by step](#5-how-it-works-step-by-step)
6. [A worked example](#6-a-worked-example)
7. [Project structure, explained](#7-project-structure-explained)
8. [The design pattern behind the scoring](#8-the-design-pattern-behind-the-scoring)
9. [How accounts and history work](#9-how-accounts-and-history-work)
10. [Why rules instead of AI/ML](#10-why-rules-instead-of-aiml)
11. [API reference](#11-api-reference)
12. [Tech stack](#12-tech-stack)

---

## 1. What is this, really?

Before anyone buys a plot of land in India, a lawyer usually digs through five or
six government records to make sure the seller actually has the legal right to
sell it, and that nothing nasty is hiding in the paperwork (an old unpaid loan
against the land, a court case, etc.).

This app automates the *first pass* of that check. You feed it a parcel of land
(who's selling it, where it is, and the outcome of 9 standard checks), and it
gives back:

- **A single score from 0 to 100** — higher is safer, exactly like a credit score.
- **A plain-English reason for every point lost**, so the score isn't a black box.

It comes pre-loaded with 20 realistic sample parcels from around Andhra
Pradesh, so you can open it and start clicking around immediately — nothing to
configure, no database to install. There's also an **"Add a property"** page
where you can enter your own combination of the 9 checks and get a real,
live-computed score back — it runs through the exact same backend logic as
every seeded property, nothing about it is pre-canned.

The app sits behind a **login**, and every property you check is recorded in a
private, per-account **History** tab — see
[section 9](#9-how-accounts-and-history-work) for how that works.

> **Note on the "20 sample parcels":** these are fictional, made-up examples
> (survey numbers, names, and locations) written to demonstrate how the
> scoring works. They are not real property records.

## 2. Running it yourself

You need **Java 17** installed. You do **not** need Maven installed — this
project bundles a Maven wrapper that downloads the right version for you
automatically the first time you run it.

**Step 1 — Clone the repository**

```bash
git clone https://github.com/sriram32086/land-title-risk-scorer.git
cd land-title-risk-scorer
```

**Step 2 — Run it**

| OS | Command |
|---|---|
| Windows | `mvnw.cmd spring-boot:run` |
| Mac / Linux | `./mvnw spring-boot:run` |

The first run takes a minute or two while it downloads Maven and the Java
libraries the project depends on — that only happens once.

**Step 3 — Open it**

Once the terminal prints `Started TitleRiskScorerApplication`, open:

👉 **http://localhost:8080**

To stop it, go back to the terminal and press `Ctrl+C`.

**Step 4 — Sign in**

The app is behind a login. A demo account is created on every startup so you
don't have to register first:

| Username | Password |
|---|---|
| `demo` | `demo1234` |

Or click **Create an account** on the login page to register your own — your
History tab is private to whichever account you're signed in as.

**Optional — look at the raw database.** The H2 web console is **off by
default**, because it runs arbitrary SQL against a database with a blank
password and this app is meant to be deployable publicly. Turn it on locally
with the `dev` profile:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Then open [http://localhost:8080/h2-console](http://localhost:8080/h2-console) —
JDBC URL `jdbc:h2:mem:titleriskdb`, username `sa`, password blank. It's an
in-memory database, so every restart resets it back to the same 20 sample
parcels; nothing you do here can break anything permanently.

## 2b. Deploying it so other people can open a link

**This is already deployed:
[land-title-risk-scorer.onrender.com](https://land-title-risk-scorer.onrender.com/)**

The repo carries everything needed to do it again from scratch: a multi-stage
`Dockerfile`, a `render.yaml` blueprint, and `server.port=${PORT:8080}` so the
app binds to whatever port the host assigns.

**On Render (free, no card):**

1. Go to [render.com](https://render.com) and sign in with GitHub.
2. **New → Web Service**, pick this repository.
3. Render reads `render.yaml`, sees `runtime: docker`, and needs nothing else — click **Create**.

First build takes a few minutes. After that you get a public URL and anyone
can sign in with the demo account.

Two honest caveats about the free tier:

- **It sleeps after ~15 minutes idle.** The first visit after a quiet spell
  takes roughly 50 seconds to wake up. Worth warning anyone you send the link to.
- **The database is in-memory**, so every restart wipes it. The 20 sample
  parcels and the `demo` account are re-seeded automatically, but accounts
  other people register — and their history — disappear on the next restart.
  Moving to Render's free Postgres would fix that; see
  [section 12](#12-tech-stack) for what else that entails.

The same `Dockerfile` works unchanged on Railway, Fly.io, Koyeb or Cloud Run.

## 3. Screens and how you move between them

There are seven pages. This is the whole app:

```mermaid
flowchart LR
    A["index.html<br/>animated intro<br/>(~2.8s, skippable)"] --> B["login.html<br/>sign in"]
    B <--> R["register.html<br/>create account"]
    B --> D["dashboard.html<br/>all properties"]
    D --> P["parcel.html<br/>one property's<br/>full breakdown"]
    D --> N["add.html<br/>score your own"]
    N --> P
    D <--> H["history.html<br/>your past checks"]
    P -.->|"logged automatically"| H
```

| Page | What it's for |
|---|---|
| `index.html` | Branded intro animation. Auto-advances to the login page; has a Skip link. Purely cosmetic — it is **not** a security gate. |
| `login.html` | Sign in. Redirects straight to the dashboard if you already have a valid session. |
| `register.html` | Create an account. Signs you in automatically on success. |
| `dashboard.html` | The main screen: summary stats plus a table of every property and its score. |
| `parcel.html` | One property: score gauge, the record itself, a weighted breakdown bar, and a card per check. Visiting it records a History entry. |
| `add.html` | Enter your own five check outcomes and get a live score. |
| `history.html` | Every property *you* have checked, most recent first. |

## 4. The 9 things it checks (in plain English)

Each of these is a real category of due diligence used when buying land in
India. If any of these terms are new to you, here's the plain-language version:

| # | Factor | Weight | What it actually means |
|---|---|---|---|
| 1 | **Section 22A prohibited list** | 18% | The state can bar a survey number from being registered at all — typically government land, land assigned to the poor, endowment, wakf or forest land. If a parcel is listed, the Sub-Registrar's system **rejects the sale deed outright**, no officer can override it, and banks decline loans against it automatically. Checked on the IGRS AP portal. |
| 2 | **Land classification (ROR-1B)** | 16% | Whether the land is ordinary private patta, or something that cannot be privately sold. Assigned land (D-Patta) transferred without the Collector's permission is **void** under the AP Assigned Lands Act 1977, and the government can resume it even after several resales. Government/poramboke, endowment and wakf land cannot pass into private hands at all. |
| 3 | **Encumbrance Certificate (EC)** | 16% | A government record listing every loan, mortgage, or legal claim ever registered against this exact piece of land. A claim follows the *land*, not the seller, so a buyer inherits it. |
| 4 | **Litigation status** | 14% | Is anyone currently suing over this land? If so the property is **"sub judice"** — a court can freeze the sale or unwind it later, no matter how clean everything else looks. |
| 5 | **Pattadar / ROR-1B ownership** | 12% | Is the person selling actually the owner recorded in the government register? If not, they may be selling on an unregistered power of attorney, which the Supreme Court held in *Suraj Lamp* conveys **no title**. |
| 6 | **Layout approval** | 9% | Has the relevant planning authority approved the layout? AP has no single body — it's **CRDA** near Amaravati, **VMRDA** around Visakhapatnam, or **DTCP** elsewhere. Unapproved layouts block construction permits and bank finance. |
| 7 | **NALA conversion** | 6% | Agricultural land must be formally converted before it can be built on. Building on unconverted land is illegal, and a refused conversion often reveals a deeper problem with the land's status. |
| 8 | **AP RERA registration** | 5% | Required for projects marketed to the public; gives buyers legal recourse. Doesn't apply to a plain land resale. |
| 9 | **MeeBhoomi record match** | 4% | Do the paper documents agree with the state's digital land record? Usually a data-entry slip rather than fraud, but it has to be fixed before registration. |

The weights add up to 100% and reflect how much legal weight each check
actually carries. The ordering is deliberate: checks 1 and 2 ask whether the
land can lawfully be sold **at all**, which is a prior question to checks 3-5
asking whether the title is clean, which in turn precede checks 6-9 about what
you may *do* with the land once you own it.

### Some rules aren't weights at all — they're ceilings

A purely additive score has a blind spot, and this project hit it hard. A
parcel clean on every documentary check but with an **active lawsuit** scored
76 and displayed as *"Low risk."* A property a court can freeze should never
read low risk, however tidy the rest of the file is.

Addition simply cannot express *"this one thing overrides everything else."*
So three conditions impose a **ceiling** on the final score rather than a
deduction:

| Condition | Ceiling | Why |
|---|---|---|
| On the Section 22A prohibited list | **5** | Registration is legally blocked. No combination of clean paperwork makes the land purchasable. |
| Government / endowment / wakf land | **5** | Cannot pass into private ownership at all. |
| Assigned land (D-Patta) | **10** | Sale without the Collector's permission is void — marginally above the inalienable categories, since assignment can be regularised. |
| Active lawsuit | **45** | Deliberately lands in "medium", not "high": a court can freeze the sale, so it must never read low risk, but a suit may prove frivolous. |

The effect is visible in the seeded data. Parcel `301/A` has flawless
paperwork and scores **82 on the weighted sum** — but it's on the 22A list, so
it reports **5**. Parcel `62/1B` is similarly clean at 84, but it's assigned
land, so it reports **10**.

Every cap is **explained on screen**, showing both the capped figure and the
weighted total it replaced, so the gauge never contradicts the arithmetic in
the breakdown below it.

Architecturally the ceiling rides on `FactorScore`, not on the `RiskFactor`
interface. Each factor declares its own; `RiskScoringService` just applies
whichever is strictest and still names no concrete factor — so a tenth factor
could introduce a ceiling without either of them changing.

## 5. How it works, step by step

Nothing here is magic — here's the exact sequence of events from the moment
you open the app to seeing scores on screen:

```mermaid
sequenceDiagram
    participant You as You (browser)
    participant FE as Frontend (dashboard.html + JS)
    participant API as Backend (Spring Boot REST API)
    participant Logic as Scoring logic (5 factor classes)
    participant DB as H2 Database

    Note over You,FE: intro plays, then you sign in
    You->>FE: land on dashboard.html
    FE->>API: GET /api/auth/me
    API-->>FE: 200 — session is valid
    FE->>API: GET /api/parcels
    API->>DB: "give me every parcel"
    DB-->>API: 20 rows of raw parcel data
    API->>Logic: score each parcel
    Logic-->>API: score + reasons for each
    API-->>FE: JSON response
    FE-->>You: renders the table you see
```

Click into any row and the same thing happens again for that one parcel, with
the full breakdown of all 9 factors instead of just the total.

The important part: **the backend and frontend only ever talk in JSON.** The
backend has no idea what the page looks like — it just answers questions like
"what's parcel #7's score?" The frontend is just JavaScript asking those
questions and drawing the answer on screen. This is exactly how a mobile app
would talk to the same backend, too.

## 6. A worked example

Let's score one real parcel from the seeded data by hand, so you can see
there's no hidden logic — it's just multiplication and addition.

**Parcel `301/A` — Bhogapuram, Vizianagaram.** Every document is in order:

| Factor | Status | Raw | Weight | Contribution |
|---|---|---|---|---|
| Section 22A prohibited list | **Listed** | 0 | × 0.18 | 0.0 |
| Land classification | Private patta | 100 | × 0.16 | 16.0 |
| Encumbrance Certificate | Clean | 100 | × 0.16 | 16.0 |
| Litigation | None | 100 | × 0.14 | 14.0 |
| Pattadar / ROR-1B | Matched | 100 | × 0.12 | 12.0 |
| Layout approval | Approved | 100 | × 0.09 | 9.0 |
| NALA conversion | Converted | 100 | × 0.06 | 6.0 |
| AP RERA | Registered | 100 | × 0.05 | 5.0 |
| MeeBhoomi match | Matched | 100 | × 0.04 | 4.0 |
| | | | **Weighted total** | **82** |
| | | | **Ceiling (22A listed)** | **5** |

Eight of the nine checks are perfect. The weighted sum is 82, which would have
displayed as *"Low risk"* — and would have been dangerously wrong, because a
22A listing means the Sub-Registrar cannot register the sale at all. The
ceiling drops it to **5**, and the detail page states why.

That gap between 82 and 5 is the entire argument for having ceilings.

## 7. Project structure, explained

This is one Maven project, but it does two distinct jobs: a Java backend that
computes scores, and a plain-JS frontend that displays them. To keep that
clear, here they are as two separate diagrams instead of one mixed tree.

### 7a. Backend — `src/main/java/com/titlerisk/`

Every request flows through these packages roughly top to bottom:

```
com.titlerisk
│
├── model/          The "nouns" of the app: Parcel (a plot of land), User (an
│                   account), ViewHistory (one recorded score check), plus the
│                   5 enums (EcStatus, LitigationStatus, …) which are fixed
│                   lists of allowed answers — an EC can only ever be CLEAN or
│                   FLAGGED, nothing else.
│
├── repository/      One interface per table (Parcel / User / ViewHistory).
│                   Talks to the database — you never write SQL here, Spring
│                   Data generates it from the method names.
│
├── service/         The "brain." RiskScoringService takes a Parcel and
│   └── factors/     produces a score by combining 9 independent checks. The
│                   factors/ subfolder has one small class per check (see
│                   section 8 for why it's split up this way).
│                   CustomUserDetailsService bridges User to Spring Security.
│
├── dto/              Shapes the backend's JSON requests and responses. Keeps
│                   the database structure (Parcel, User) separate from what
│                   actually goes out over the wire — no password hash can
│                   leak into a response, because no DTO has that field.
│
├── controller/       The only classes that know about HTTP:
│                   • ParcelApiController  — GET/POST /api/parcels
│                   • AuthController        — register / login / logout / me
│                   • HistoryApiController  — GET /api/history
│
└── config/           SecurityConfig — which routes need a session, BCrypt setup.
                    DataSeeder    — inserts the 20 sample parcels and the
                                     demo account on startup.
```

### 7b. Frontend — `src/main/resources/static/`

Plain files, no build step, no npm, served as-is by Spring Boot. One HTML file
and one JS file per screen, plus shared helpers:

```
static/
├── index.html  login.html  register.html  dashboard.html
├── parcel.html  add.html  history.html
├── css/style.css      one stylesheet, shared by every page
└── js/                one file per page, plus common.js
```

`js/common.js` holds everything the pages share: `requireAuth()`, the top nav
bar, the SVG icon set, and the plain-English factor glossary. Every other JS
file drives exactly one page:

| Page | Its script |
|---|---|
| `index.html` (intro) | `js/intro.js` |
| `login.html` | `js/login.js` |
| `register.html` | `js/register.js` |
| `dashboard.html` | `js/dashboard.js` |
| `parcel.html` | `js/detail.js` |
| `add.html` | `js/add.js` |
| `history.html` | `js/history.js` |

### 7c. Everything else

| File | What it's for |
|---|---|
| `src/main/resources/application.properties` | App settings — port, database URL, etc. |
| `pom.xml` | Tells Maven which libraries this project depends on, and how to build it. |
| `mvnw` / `mvnw.cmd` | Lets you build/run the project without installing Maven yourself — see [section 2](#2-running-it-yourself). |

## 8. The design pattern behind the scoring

This is the part that's actually interesting from a software design point of
view, so it's worth walking through slowly.

**The naive way to write this** would be one big method with an if/else (or
switch) for every factor, something like:

```java
// what this project deliberately avoids:
double score = 0;
if (ec == CLEAN) score += 30; else score += 4.5;
if (litigation == NONE) score += 25; else if (litigation == PENDING) score += 12.5; ...
// ...and so on for all 9 factors, all tangled together in one method.
```

The problem: every time you add a 10th check (say, a coastal regulation zone check),
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

The payoff: to add a 10th factor, you write **one new file** and mark it
`@Component`. Spring finds it automatically and `RiskScoringService` starts
including it in the total — without a single line of `RiskScoringService`
changing. In software design terms, this is the **Open/Closed Principle**:
the scoring engine is *open* to adding new checks, but *closed* to needing
modification when you do.

## 9. How accounts and history work

**Accounts.** Registration stores a username and a **BCrypt hash** of the
password — the plain-text password is never stored, logged, or returned by any
endpoint. Signing in creates an ordinary server-side session; the browser
holds a `JSESSIONID` cookie and sends it automatically on every later request,
so the frontend never has to manage a token.

**How pages are protected.** Static files (every `.html`, `css/`, `js/`) are
always servable — a browser must be able to load `login.html` before it can
log in. The real gate is on the data: every `/api/parcels/**` and
`/api/history/**` call requires a valid session. Each protected page calls
`requireAuth()` in `common.js` on load, which asks `GET /api/auth/me` and
bounces to `login.html` if that returns 401. So the app is secured at the API,
not by hiding HTML files.

**History.** Whenever a signed-in user opens `GET /api/parcels/{id}`, the
backend writes a `ViewHistory` row — username, which parcel, and a *snapshot*
of the score and risk band at that moment. Snapshotting is deliberate: History
is a record of what you saw when you checked it, not a live re-score, so the
History page never has to re-run the scoring engine to render a list.

```mermaid
sequenceDiagram
    participant U as You
    participant FE as Frontend
    participant API as Backend
    participant DB as H2

    U->>FE: open parcel.html?id=6
    FE->>API: GET /api/auth/me
    API-->>FE: 200 {username}
    FE->>API: GET /api/parcels/6
    API->>DB: load parcel + write ViewHistory row
    API-->>FE: score + 5 factor explanations
    FE-->>U: detail page
    U->>FE: click History
    FE->>API: GET /api/history
    API->>DB: rows WHERE username = you
    API-->>FE: your checks, newest first
```

Queries are always scoped to the session's own username, so there is no way to
read another account's history through the API.

## 10. Why rules instead of AI/ML

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

## 11. API reference

The backend is a plain JSON REST API. You can call it directly with `curl`,
Postman, or from any other app — it doesn't know or care that the bundled
frontend exists.

Everything except the auth endpoints requires a session, so with `curl` you
need a cookie jar:

```bash
# sign in once, keep the session in cookies.txt
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo1234"}'

# then send it with every later call
curl -b cookies.txt http://localhost:8080/api/parcels
```

<details>
<summary><b>Auth</b> — <code>POST /api/auth/register</code>, <code>/login</code>, <code>/logout</code>, <code>GET /api/auth/me</code></summary>

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"jane","password":"secret123"}'
```

All four return `{"username":"jane"}` on success (`/logout` returns an empty
200). Registering also signs you in, so there's no second login step.

| Situation | Status | Body `message` |
|---|---|---|
| Username under 3 chars | `400` | `Username must be at least 3 characters.` |
| Password under 6 chars | `400` | `Password must be at least 6 characters.` |
| Username already exists | `409` | `That username is already taken.` |
| Wrong username/password | `401` | `Invalid username or password.` |
| Calling `/me` with no session | `401` | `Not authenticated. Please log in.` |
</details>

<details>
<summary><b>History</b> — <code>GET /api/history</code></summary>

```bash
curl -b cookies.txt http://localhost:8080/api/history
```

```json
[
  {
    "id": 2,
    "parcelId": 9,
    "surveyNo": "45/2A",
    "locationArea": "Anandapuram, Visakhapatnam",
    "score": 46,
    "riskBand": "medium",
    "viewedAt": "2026-08-15T00:16:09.897025Z"
  }
]
```

Newest first, and always scoped to the signed-in account.
</details>

<details>
<summary><code>GET /api/parcels</code> — list every parcel with its score</summary>

```bash
curl -b cookies.txt http://localhost:8080/api/parcels
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
curl -b cookies.txt http://localhost:8080/api/parcels/1
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
  "uncappedScore": 100,
  "ceilingReason": null,
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
curl -b cookies.txt -X POST http://localhost:8080/api/parcels \
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

## 12. Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3 (Spring Web, Spring Data JPA) |
| Auth | Spring Security — session-based login, BCrypt password hashing |
| Database | H2 (in-memory — no install, no setup) |
| Frontend | Plain HTML, CSS, and JavaScript (`fetch` API) — no framework, no build step, no npm |
| Icons | Hand-written inline SVG (no icon library, no emoji) |
| Testing | JUnit 5 — 50 tests, run with `mvnw.cmd test` |
| Build tool | Maven (wrapper included, so a local Maven install isn't required) |

---

<p align="center"><sub>Built as a portfolio project. Sample data is fictional.</sub></p>
