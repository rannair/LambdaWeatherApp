# LambdaWeatherApp

This is a sample serverless  application   to store and retrieve data from DynamoDb.


## Deploy the sample application

The Serverless Application Model Command Line Interface (SAM CLI) is an extension of the AWS CLI that adds functionality for building and testing Lambda applications. It uses Docker to run your functions in an Amazon Linux environment that matches Lambda. It can also emulate your application's build environment and API.

To use the SAM CLI, you need the following tools.

* SAM CLI - [Install the SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
* AWS CLI -[Install and configure AWS CLI]https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html
* Java11 - [Install the Java 11](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html)
* Maven - [Install Maven](https://maven.apache.org/install.html)
* Docker - [Install Docker community edition](https://hub.docker.com/search/?type=edition&offering=community)

To build and deploy your application for the first time, run the following in your shell:
1) Build  and package the  application using **mvn clean package**
2) validate the sam template using  **sam validate -t template.yaml**
3) create a s3 bucket for SAM to manage lambda artifacts by running **aws s3api create-bucket --bucket <BUCKET_NAME> --region us-east-1**
4) Deploy the application using sam deploy **--s3-bucket <BUCKET_NAME>  --stack-name lambda-weather-pp  --capabilities CAPABILITY_IAM**

To test the application after deployment:

Get the POST invoke URl from API gateway console and replace in curl command 

Insert data by executing below statements:
curl --location 'https://g1qwvydbjf.execute-api.us-east-1.amazonaws.com/Prod/events' \
--header 'Content-Type: application/json' \
--data '{"locationName":"Brooklyn, NY", "temperature":91,
"timestamp":1564428897, "latitude": 40.70, "longitude": -73.99}'

curl --location 'https://g1qwvydbjf.execute-api.us-east-1.amazonaws.com/Prod/events' \
--header 'Content-Type: application/json' \
--data '{"locationName":"Oxford, UK", "temperature":64,
"timestamp":1564428898, "latitude": 51.75, "longitude": -1.25}'

Query the data inserted in above commands by invoking executing below statement:

curl --location 'https://g1qwvydbjf.execute-api.us-east-1.amazonaws.com/Prod/locations'

Tear down stack after usage :
To tear down  the stack by running the command from cli **aws cloudformation delete-stack --stack-name LambdaWeatherApp**