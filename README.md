
# Fdx-Mock-Auth-Server

## Description
This project helps banks/financial institutions (FIs) implement an auth server in their organization. While you can choose any auth server, OAuth2.0 or OIDC is preferred. All responses conform to the FDX 6.0 standard.

## Prerequisites
1. An IDE that supports Java 17 or above (e.g., Eclipse, IntelliJ, STS).
2. Java installed on your local system.
3. Postman installed on your local system.
4. Docker: Install Docker (Required only for running in docker)

## Installation Instructions
1. Download the project from GitHub.
2. Import the project into your preferred IDE as an existing Maven project.
3. Run the application as a Java Application.
4. Once the project is running, download the Postman collection from the resource/postman folder. 
5. Import the downloaded Postman collection into Postman. 
6. You can now use the imported collection to send requests to the API endpoints and observe the responses.

## Usage
**Note:** To run through consent journey and resource APIs skipping DCR, follow below steps with default client id: dh-fdx-client-registrar-2
1. Run the request from Postman: `Authorize (via PAR+RAR)`.
2. Copy the login URL from the `PAR Authorize` request curl section.
3. Paste the login URL in your browser to start the consent journey.
4. Log in with a valid user (fdxuser, fdxuser1, fdxuser2). After a successful login, you can select the accounts for which you want to give consent.
5. Once the account is selected, hit the submit button.
6. Upon successful consent, you'll receive a success message. Copy the authorization code (the "code" field) from the URL in the browser.
7. Paste the authorization code into the body of the Get Access Token request, under the "code" key.
8. Hit the `Get Access Token` request endpoint. You will get the authorization token.
9. Now you can access the resource API using this authorization token.

## Instructions for Building a Docker Image
1. Uncomment the below properties so that the authorization server can interact with the resource server running on the docker container.
   1. mock.res.server.baseurl=http://fdx.mock.resource.server:9090
   2. mock.res.server.userurl=http://fdx.mock.resource.server:9090/user
2. Build the Docker image using the Docker build command. Run this command from the directory containing the Dockerfile. 
   1. "docker build -t fdx-mock-auth-server ."
3. Verify that the Docker image was successfully created. You should see <image-name> listed in the output.
4. Since both servers will be running on Docker containers, their hosts will be localhost. 
   Each Docker container runs in its own isolated network namespace. The localhost within docker1 refers to docker1 itself, not to docker2. They will not be able to interact with each other.
5. To establish communication between Docker containers (docker1 and docker2) using Docker's port mapping (publishing ports).
   Create a user-defined bridge network, make sure both containers are connected to the same network and that they can resolve each other's container names.
6. Commands to create a custom network and run the containers are :
   1. `docker network create my-network`
   2. `docker run -d --network=my-network -p 8080:8080 --name fdx.mock.auth.server fdx-mock-auth-server`
   3. `docker run -d --network=my-network -p 9090:9090 --name fdx.mock.resource.server fdx-mock-resource-server`

## License
This is an open-source project and does not have any specific licensing.

## Contact Information
For any queries, please post a comment on GitHub. We will look into it and get back to you.
