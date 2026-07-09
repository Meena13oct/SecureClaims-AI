# SecureClaims AI - CloudFormation Template

## Quick Start

### Prerequisites
1. AWS CLI configured (`aws configure`)
2. EC2 Key Pair created in ap-south-1
3. GitHub PAT with `read:packages` scope

### Create Key Pair (if needed)
```bash
aws ec2 create-key-pair --key-name secureclaims-keypair --query 'KeyMaterial' --output text --region ap-south-1 > secureclaims-keypair.pem
```

### Deploy Stack
```bash
aws cloudformation create-stack \
  --stack-name secureclaims-ai \
  --template-body file://infrastructure/cloudformation/secureclaims-stack.yaml \
  --parameters \
    ParameterKey=KeyPairName,ParameterValue=secureclaims-keypair \
    ParameterKey=DBPassword,ParameterValue=YourDBPass123 \
    ParameterKey=JWTSecret,ParameterValue=your-production-jwt-secret-at-least-32-chars \
    ParameterKey=GitHubPAT,ParameterValue=ghp_your_token_here \
  --capabilities CAPABILITY_IAM \
  --region ap-south-1
```

### Check Status
```bash
aws cloudformation describe-stacks --stack-name secureclaims-ai --region ap-south-1 --query 'Stacks[0].StackStatus'
```

### Get Outputs
```bash
aws cloudformation describe-stacks --stack-name secureclaims-ai --region ap-south-1 --query 'Stacks[0].Outputs' --output table
```

### Delete Stack
```bash
aws cloudformation delete-stack --stack-name secureclaims-ai --region ap-south-1
```

## Detailed Guide
See `.kiro/outputs/aws-cloudformation-guide.md` for full documentation.
