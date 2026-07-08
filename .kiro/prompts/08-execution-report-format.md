# Execution Report Format

After completing the user story implementation, generate a detailed EXECUTION REPORT.

Include the following sections:

## 1. User Story Summary

* User Story ID and Title
* Short description of functionality implemented

## 2. Functional Overview

* What the feature does
* Key business logic implemented
* Edge cases handled

## 3. API Details

For each endpoint:

* Method (GET/POST/PUT/DELETE)
* URL
* Request body (JSON)
* Response body (JSON)
* HTTP status codes

## 4. Database Changes

* Tables created (with schema)
* Tables updated (if any)
* Relationships (FKs)

## 5. Data Inserted

* Whether seed/test data was inserted (Yes/No)
* If yes, provide:

  * Table name
  * Insert statements
  * Sample records

## 6. Postman Testing Guide

Provide ready-to-use test data:

* Endpoint URL
* Headers (if any)
* Sample request JSON
* Expected response JSON

Format it in json format and apply beautification, so that it can be directly used in Postman and use api-design-output for the test data to be generated. There should be relationship between the test data. 

## 7. Test Coverage

* Unit tests created
* What scenarios are covered

## 8. Notes / Assumptions

* Any assumptions made
* Any limitations

---

## Output Instructions

IMPORTANT:
* Keep report structured and readable
* Do NOT skip any section
* Ensure Postman data is realistic and usable

After generating the execution report, SAVE it as a Markdown file in the project.

File path format:
```
docs/reports/epics/<epic-name>/<user-story-id>-report.md
```

Epic name mapping (use kebab-case):
| Epic | Folder Name |
|------|-------------|
| Epic 1 – Foundation & Infrastructure | `foundation-infrastructure` |
| Epic 2 – Identity Service | `identity-service` |
| Epic 3 – Claims Service | `claims-service` |
| Epic 4 – Fraud Detection Service | `fraud-detection-service` |
| Epic 5 – Notification Service | `notification-service` |
| Epic 6 – Admin Endpoints | `admin-endpoints` |
| Epic 7 – Cross-Cutting & Testing | `cross-cutting-testing` |

Example:
```
docs/reports/epics/foundation-infrastructure/US-001-report.md
docs/reports/epics/identity-service/US-003-report.md
```

Ensure:
* Folder structure is created if it does not exist
* File is clean Markdown (.md)
* Use proper headings and code blocks
* File name includes user story ID

Do NOT overwrite existing reports unless explicitly instructed.
STOP after generating and saving the report.
