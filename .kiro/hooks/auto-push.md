# Auto-Commit & Deploy Hook
trigger: "on_save"
files: [
  "src/main/resources/db/migration/*.sql", 
  "**/pom.xml",
  "**/*.java",
  "**/*.yml",
  "**/*.yaml",
  "**/Dockerfile"
]

# Instruction
Whenever any core file is saved—including database migrations, Maven files, Java source code, Spring configuration properties, microservice Dockerfiles, or any YAML configurations (like GitHub actions pipeline or application manifests)—verify local code health. If everything passes, automatically stage, commit, and push the updates directly to the remote main branch via your GitHub App connection.

