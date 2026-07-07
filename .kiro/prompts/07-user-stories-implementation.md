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
