# pigeon-post
pigeon-post is an api to send and receive messages. Only an authenticated user would be able to access the endpoints.
API accepts request in the form of json. It accepts three parameters. Here's the sample request.

`{
    "from": "1234",
    "to": "value",
    "text": "test"
}`

API has an ability to register for DND. When a STOP message is being sent, sender will no longer be able to send messages to the recipient. And it also sets a limitation of 50 messages per day for a sender.

# Installation

The application is developed using spring boot, gradle, Java. And it's backed by the datasource postgres and redis for caching.

 **Install Java**
        
    brew cask install java8
    Set JAVA_HOME
 
 **Install gradle**
 
    brew install gradle
    
Follow the steps below if you would like to replicate the production environment locally. Else you can skip.
 
 **Install postgres**
 
    brew install postgresql
    brew services start postgresql
    createuser -s postgresadmin
    createdb -T template0 sms
    psql sms < /{PATH}/schema.sqlio
  Your DB string should be:
    `jdbc:postgresql://localhost:5432/sms`
 
 **Install redis**
    `brew install redis`

# Setting up the local environment

     export DB_URL=jdbc:postgresql://localhost:5432/sms
     export DB_USERNAME= 
     export DB_PASSWORD=
     export REDIS_HOST=127.0.0.1
     export REDIS_PORT=6379
     
Alternatively you can also execute:
     `source setup_env.sh`

Note: Leave DB_USERNAME and DB_PASSWORD to be empty.
    
# Run application

    ./gradlew bootRun -PspringProfile=dev
    
# Sample Request:

    METHOD: POST
    URL: http://localhost:5000/message/send
    Authorization: Basic cGxpdm8xOjIwUzBLUE5PSU0= (This will change based on the user)
    Body:
        `{
            "from": "1234",
            "to": "value",
            "text": "test"
        }`
Same sample request can be used for receiving messages with the URL:
     
     URL: http://localhost:5000/message/receive
    
# Run tests
    
    ./gradlew test -PspringProfile=dev
    
The application has been deployed in Amazon Web Services.
 
