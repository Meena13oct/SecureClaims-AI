You are implementing the system based on .kiro/architecture/03-user-stories-output.md.

# Follow these strict rules:
 - FOCUS ONLY ONE EPIC AT A TIME IN THE ORDER SPECIFIED
 - STRICT RULES:
    - Work ONLY on user stories belonging to this epic
    - Do NOT touch other epics
    - Process ONE user story at a time
    - Maintain consistency across all stories in this epic
    - Reuse previously created components within this epic

# 1. Process ONLY ONE user story at a time.
# 2. For each user story, follow this order:
## Step 1: Analyze the user story
   - Extract requirements
   - Identify actors and actions
   - Identify validations and edge cases

## Step 2: API Design
   - Identify API endpoints using .kiro/architecture/04-api-design-output.md

## Step 3: Generate database schema
   - Identify database entities
   - SQL table creation script
   - Constraints, indexes, relationships  

## Step 4: Generate backend code
   - Spring Boot controller
   - Service layer
   - Repository layer
   - DTOs and entities

## Step 5: Generate test cases
   - Unit tests (JUnit + Mockito)
   - Integration tests (if needed)

## Step 6: Generate seed data
   - SQL insert scripts or data.sql

# 3. Implementation order
   - Start with the FIRST user story in .kiro/architecture/03-user-stories-output.md
   - Full implementation (analysis → DB → tests → seed data)
   - Maintain consistency with previously generated code
   - Reuse existing entities and schemas where applicable   

# 4. Review the previously generated user story implementation. Fix the following issues:
    - Ensure code compiles correctly
    - Correct any missing validations
    - Fix incorrect API design
    - Align database schema with entities
    - Improve test coverage

# 5. After completing ONE story:
   - Do NOT move to the next story.
   - Only fix and regenerate the current story.
   - Stop and wait for confirmation before proceeding to next story
   
# 6. Ensure:
   - Production-ready code
   - Proper exception handling
   - Logging included
   - Validation annotations used
   - Clean architecture followed

# 7. After implementing the a user story generate a report as specified in .kiro/prompts/08-execution-report-format.md

# 8. After the report is generated in step 7, automatically create Postman test cases:
   - Read the "Postman Testing Guide" section from the generated report
   - Read `.postman.json` to get workspace ID, collection IDs, and base URLs
   - Service-to-collection mapping (all pre-created in `.postman.json`):
     - identity-service → `identityservice` collection (port 8081)
     - claims-service → `claimsservice` collection (port 8082)
     - fraud-detection-service → `frauddetectionservice` collection (port 8083)
     - notification-service → `notificationservice` collection (port 8084)
   - Use the `id` field (NOT `uid`) from `.postman.json` when calling `createCollectionRequest`
   - For each test case in the Postman Testing Guide section:
     - Create a request in the matching service collection using `createCollectionRequest`
     - Include method, URL, headers, request body (rawModeData), and test scripts (pm.test assertions)
     - Test scripts must validate: status code, response structure, and business logic
   - After all requests are created, attempt to run the collection using `runCollection`
   - Update `.postman.json` with any new request IDs under the appropriate collection's `requests` object
   - The test cases must be visible, in json format and runnable from the Postman app. 
   - For example, they should be displaye in the below format:
    {
    "firstName": "Jane",
    "lastName": "Boe",
    "email": "jane.boe@example.com",
    "username": "janeboe",
    "password": "SecureP@ss2"
    }  