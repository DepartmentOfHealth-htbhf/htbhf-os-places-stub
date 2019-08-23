# htbhf-os-places-stub
Service for stubbing out the os-places API.
By default this app runs on port 8150, set a SERVER_PORT environment variable to override. 

Postcodes are returned from requests in the following format:

http://localhost:8150/places/v1/addresses/postcode?postcode=BS161A&key=foo

Responses are keyed from postcode.

## Stubbing
The service uses Wiremock to serve stubbed responses. See src/main/resources/mappings.