# SecureClaims AI - Kiro Setup
Generate detailed requirements for SecureClaims AI based on the ADLC prompt in .kiro/prompts/01-requirements-generation.md to .kiro/outputs/01-requirements-output.md and use only the technology stack specified in 01-requirements-generation.md 
Generate architecture diagram based on .kiro/prompts/02-architecture-design.md to .kiro/architecture/02-architecture-design-output.md 
Generate api-design.md based on .kiro/architecture/02-architecture-design-output.md 
Generate user stories based on the .kiro/prompts/user-stories.md 
Generate api deisgn based on .kiro/architecture/03-user-stories-output.md for each of the user stories to kiro/architecture/04-api-design-output.md
Generate  maven project structure into D:\Meena_1\CapstoneProject\Project\SecureClaims-AI for SpringBoot Microservice project for the technology stack defined in 01-requirements-output.md
Generate Coding Standards .kiro/steering/coding-standards.md for Spring Boot Microservice project and it should follow Best Practices and design pringciple
Generate the database scripts for each of the service for creating the table and include it in resource folder as dbScripts
Use .kiro/prompts/05-generate-pipeline.md to create ci/cd pipeline
//Configure Github Secrets -- refer to Settings.docx
06-generate-docker-files.md
Since you are now connected to my GitHub account via the official app, please target my `SecureClaims-AI` repository. Automatically commit and push all local changes—including the `.github/workflows/ci-cd.yml` file and the 4 new microservice `Dockerfiles`—directly to my remote `main` branch.
Yes, I confirm. Please proceed with pushing directly to the main branch for this project setup.
Please stage, commit, and push my new .kiro/hooks/auto-push.md file to the main branch. Once this is pushed, confirm that my workspace auto-deploy hook is officially.
Our GitHub Actions pipeline run #5 just failed. Please use your GitHub tool to inspect the latest workflow logs for run #5, find out exactly which microservice compilation or test is causing the failure, and modify the code or configurations locally to fix it.







