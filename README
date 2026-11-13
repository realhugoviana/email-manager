# Email Manager

A multi-agent email assistant. It watches a mailbox, routes each incoming email to a specialised LLM agent, and shows you the drafted reply in a web page where you can edit it, send it, or discard it. Nothing is ever sent without your approval.

The language models run locally in [LM Studio](https://lmstudio.ai). The project was built around the mailbox of a university professor, so the bundled agents are written for that context (students, colleagues, administration, reviewers, and so on).

> **LM Studio only.** The app is designed and tested exclusively with LM Studio as the model provider. Using another provider may require adapting the app — see [Using another provider](#using-another-provider).

## How it works

```
 IMAP inbox ──► EmailService ──► DispatcherAgent ──► AnswererAgent ──► Web UI ──► SMTP reply
 (IMAP IDLE)    (queue, one      (embedding           (chat model       (you accept,
                 email at a       similarity)          drafts reply)     edit or discard)
                 time)
```

1. **Listening.** `EmailService` opens the inbox over IMAP and waits with IMAP IDLE. Each email that arrives *while the app is running* is put in a queue; emails already in the inbox at startup are ignored. Emails are handled one at a time.
2. **Dispatching.** `DispatcherAgent` embeds the email's subject and body with the embedding model, then compares it (cosine similarity) with the embedding of every answerer agent. The closest agent gets the email.
3. **Answering.** The chosen `AnswererAgent` adds the email to its own running context (its memory of the emails it has handled) and asks the chat model to draft a reply using its system prompt plus that context.
4. **Review.** The draft appears in the web UI. You can edit the text, then **Send reply** or **Discard**. The processing of the next email waits for your decision.
5. **Sending.** An accepted reply is sent over SMTP to the original sender, threaded as a reply (`Re:` subject, `In-Reply-To` / `References` headers).

### The agents

The answerer agents are defined in [src/main/resources/answerer_agents.json](src/main/resources/answerer_agents.json). Each entry has a name, a system prompt and an optional starting context:

```json
{
  "agentName": "studentAgent",
  "systemPrompt": "Tu es un assistant chargé de rédiger les réponses aux courriels des étudiants ...",
  "initialContext": ""
}
```

Twelve agents are bundled: `administrativeAgent`, `conflictAgent`, `criticalAgent`, `externalAgent`, `externalProfessorAgent`, `institutionalAgent`, `noAnswerAgent`, `personalAgent`, `professorAgent`, `researcherAgent`, `reviewerAgent` and `studentAgent`. To add, remove or retune an agent, edit the JSON file and restart the app — no code change is needed.

Because routing compares the email with each agent's system prompt *and* context, an agent becomes a better match for a conversation it has already taken part in. This memory lives only in RAM and is reset on every restart.

## Requirements

- **Java 25**
- **LM Studio** with a chat model and an embedding model downloaded
- A mailbox reachable over **IMAP and SMTP** (the example below uses Gmail)
- Maven is *not* required: the included wrapper (`./mvnw`) downloads it

## Setup

### 1. LM Studio

1. Download the two models in LM Studio:
   - chat model: `google/gemma-4-e4b`
   - embedding model: `text-embedding-nomic-embed-text-v1.5`
2. Load both models.
3. Start the local server (**Developer** tab → **Start server**). It must listen on the default port, `1234`.

LM Studio must be running with both models available **before** the app starts: at startup the app computes one embedding per agent, and an agent whose embedding fails is not registered.

The model names in `application.properties` must match the identifiers LM Studio shows for the models. You can list them with:

```bash
curl http://localhost:1234/v1/models
```

### 2. Gmail

Gmail does not accept your normal password over IMAP/SMTP; you need an **app password**.

1. Turn on 2-Step Verification on the Google account.
2. Create an app password at <https://myaccount.google.com/apppasswords>. Google shows a 16-character password in four groups, such as `abcd efgh ijkl mnop`.
3. Make sure IMAP access is enabled for the account (Gmail → Settings → *Forwarding and POP/IMAP*).

Using a dedicated test address is a good idea: the app replies from this account.

### 3. `application.properties`

All configuration lives in [src/main/resources/application.properties](src/main/resources/application.properties). A complete example with a Gmail address and the LM Studio setup described above:

```properties
# --- IMAP setup (incoming email) ---
email-manager.imap.host=imap.gmail.com
email-manager.imap.port=993
email-manager.imap.username=your.address@gmail.com
email-manager.imap.password=abcd efgh ijkl mnop
email-manager.inbox.folder=INBOX

# --- SMTP setup (outgoing email) ---
email-manager.smtp.host=smtp.gmail.com
email-manager.smtp.port=587
email-manager.smtp.username=your.address@gmail.com
email-manager.smtp.password=abcd efgh ijkl mnop

# --- LM Studio (Spring AI) setup ---
spring.ai.openai.api-key=dummy
spring.ai.openai.base-url=http://localhost:1234/v1
spring.ai.openai.chat.model=google/gemma-4-e4b
spring.ai.openai.embedding.model=text-embedding-nomic-embed-text-v1.5
```

| Property | Meaning |
|---|---|
| `email-manager.imap.host` / `.port` | IMAP server, over SSL. For Gmail: `imap.gmail.com` / `993`. |
| `email-manager.imap.username` / `.password` | The mailbox address and its app password. |
| `email-manager.inbox.folder` | Folder to watch. `INBOX` for the main inbox. |
| `email-manager.smtp.host` / `.port` | SMTP server, with STARTTLS. For Gmail: `smtp.gmail.com` / `587`. |
| `email-manager.smtp.username` / `.password` | Account the replies are sent from — normally the same address and app password as IMAP. |
| `spring.ai.openai.api-key` | LM Studio does not check it, but Spring AI requires a value. Any placeholder such as `dummy` works. |
| `spring.ai.openai.base-url` | LM Studio's OpenAI-compatible endpoint. |
| `spring.ai.openai.chat.model` | Model that drafts the replies. |
| `spring.ai.openai.embedding.model` | Model used to route emails to agents. |

Replace the address and password with your own, and do not commit real credentials.

## Running

Start the app **from the project root** (the agents file is loaded through a path relative to it):

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`.

The console prints `Successfully initialized agent: ...` once per agent. Then open <http://localhost:8080>.

## Using it

1. Leave the page open. The badge in the header reads **Waiting for emails**.
2. Send an email to the configured address. It appears under **Current email** and the status becomes **Generating reply**.
3. When the draft is ready, the status becomes **Awaiting your decision** and the draft shows under **Proposed reply**. Edit it if you want.
4. Click **Send reply** to send it to the sender, or **Discard** to drop it.
5. The result is shown (**Sent**, **Discarded** or **Failed**) and the app moves on to the next queued email. The header shows how many emails are waiting.

Good to know:

- The page updates live; there is no need to refresh. Several tabs can be open, and the first decision wins.
- The reply goes only to the sender of the original email, not to people in CC.
- For spam, phishing or purely informational emails, the `noAnswerAgent` is meant to produce no reply. In that case the UI shows **Failed** with "The agents did not produce a reply" — this is expected, and nothing is sent.
- Reply time depends on your hardware and on how much context the chosen agent has accumulated.

## Using another provider

The app talks to the models through Spring AI's OpenAI starter, pointed at LM Studio's OpenAI-compatible server, with a placeholder API key and with the chat model and the embedding model served from the same base URL. It has only been run in that configuration.

Another provider may need changes: different `spring.ai.*` properties (a real API key, separate endpoints for chat and embeddings), a different Spring AI starter in [pom.xml](pom.xml), or adjustments in [LMService.java](src/main/java/com/lesageviana/email_manager/service/LMService.java). The agent prompts were also written and evaluated against the Gemma model above, so other models can route and answer differently.

## Project layout

```
src/main/java/com/lesageviana/email_manager/
├── EmailManagerApplication.java   Spring Boot entry point
├── agent/
│   ├── IAgent.java                Common agent interface
│   ├── DispatcherAgent.java       Routes an email to the closest answerer agent
│   └── AnswererAgent.java         Drafts a reply from its system prompt and context
├── config/AgentConfig.java        Builds the answerer agents from answerer_agents.json
├── model/EmailMessage.java        Email data passed between components
├── service/
│   ├── EmailService.java          IMAP listener, queue and processing pipeline
│   ├── LMService.java             Chat and embedding calls (Spring AI)
│   ├── OutgoingMailService.java   Sends replies over SMTP
│   └── ReplyDecisionService.java  Bridge between the pipeline and the web UI
└── ui/                            Vaadin web UI (review page)
src/main/resources/
├── application.properties         Mailbox and LM Studio configuration
└── answerer_agents.json           Agent definitions
experiments/                       Routing evaluation
```
