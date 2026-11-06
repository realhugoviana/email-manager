# Fake university inbox - 156 e-mails (.eml)

A realistic, fully fictional received-mailbox for Pr. Arno Lesage,
professor of computer science at the (fictional) Universite de Valmont,
France. The corpus spans 28 September - 24 October 2026.

## Files

- `eml/` - 156 individual RFC-5322 e-mail files (UTF-8, CRLF line endings,
  MIME-B-encoded accented subjects, valid unique Message-IDs, real
  In-Reply-To / References threading, 4 messages with base64 attachments:
  2 x .ics + 2 x .txt, including the EU letter of intent).
- `inbox.mbox` - the same 156 messages concatenated in one mbox file
  (messages in chronological order, `From MAILER-DAEMON` separators,
  body `From ` lines escaped per mboxrd convention).
- `index.csv` - one row per message: num, filename, datetime, category,
  thread (root Message-ID), from, subject (decoded UTF-8).
- `categories.txt` - `NNN category` map used to build the index.

## Distribution (156 messages)

| Category | Count | Share |
|---|---|---|
| Spam / phishing | 7 | 4.5% |
| Unrelated (2FA codes, wrong recipient, personal, auto-replies) | 23 | 14.7% |
| Information / newsletters (no answer needed, French then English) | 30 | 19.2% |
| Student questions and student situations | 31 | 19.9% |
| Work / research conversations | 46 | 29.5% |
| Other (particular needs, complex threads, media, institutions...) | 19 | 12.2% |

## Persona and recurring cast (all fictional)

- **Pr. Arno Lesage** <arno.lesage@univ-valmont.fr> - CS professor, M1
  programme coordinator, member of lab LIAV (themes: parallel
  programming, GPU computing, energy-aware LLM serving).
- Administration: Helene Vasseur (direction des etudes), Christine Moreau
  (LIAV secretariat), Karim Benali (scolarite), the DSI, DRH, ED SIM,
  BU, SUAPS, communication services, Mathilde Roux (mission handicap).
- Colleagues: Sylvain Rambert (PR, HPC/VEGA), Beatrice Nguyen (MCF),
  Julien Perrot (MCF).
- PhD students: Raphael Vinot (3rd year), Louna Berthier (2nd year).
- ~30 students, French- and English-speaking (exchange students from
  Redmarch, St Kieran's, TU Heldring...), including one student
  threatening the professor after an exam-fraud referral (e-mail 154).
- International partners: Sarah Whitfield (Univ. of Aldergate, UK -
  joint paper, also coordinator of the EFFICIENT-HPC doctoral network),
  Miguel Santos (UBL Lisbon - plenary invitation), Daniel Okafor (ITK
  Kano - sabbatical visit), Elena Marchetti (PC chair, SCALE-UP 2027),
  Richard Aldous (JHPDA journal editor), Marc Le Goff (Univ. de
  Pontcroix - CIFRE project), Paul Esteve (IRIS Saint-Martin,
  THERM-700 dataset).
- Institutional mail: Francoise Lemoine (Mesocentre de l'Universite de
  Ravel - MURAL compute-sharing consortium, e-mail 151), Dr. Annika
  Sorensen (European Research Executive Agency - EFFICIENT-HPC
  doctoral network, e-mail 153).
- Media and associations: Elise Fontaine (Le Mensuel de Valmont,
  reportage on the daily life of a professor, e-mail 155), Recyclo
  Valmont / Repair Cafe (open-day request, e-mail 156).
- Fictional companies: Kerdata (Xavier Dollat, R&D director), Kelvix,
  Gridpulse.

## Threads

Multi-message conversations use genuine In-Reply-To / References chains,
for example: the Whitfield paper (5 messages), the Kaminski exam-clash
thread (student + direction des etudes), the Collet and Ferber student
support threads, the NEPTUNE funding thread, the JHPDA review thread,
the SCALE-UP PC thread, the Lisbon trip thread. Replies written by the
professor appear quoted with `>` inside follow-up messages: the corpus
contains only received mail.

## Importing into Thunderbird

- All 156 at once: install *ImportExportTools NG*, right-click a folder,
  choose ImportExportTools NG > Import mbox file, pick `inbox.mbox`.
- By file: ImportExportTools NG > Import all messages from a directory,
  pick `eml/` (individual .eml files can also be dragged onto a folder).

## Disclaimer

Everything here is fictional: people, addresses, domains, events,
companies, papers and grants. Do not send mail to these addresses, and
do not click the links in the spam/phishing messages - several messages
intentionally mimic real phishing patterns (fake invoice, parcel fees,
password expiry, lottery) so the corpus can be used for security
awareness or mail-client testing. E-mail 154 portrays an aggrieved
student making legal and veiled threats after an exam-fraud referral;
it is fictional dialogue, not a real safety concern.
