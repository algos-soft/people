# Guideline for candidate

Candidate has to create an endpoint for importing users data from a CSV file to proof his/her programming skills.

## Game Rules

In order to maintain things simple, the application has to follow OOP approach, so no specific framework knowledge is needed.

The application has to be stable.

No skeleton is provided. Developer can provide any solution to proof his/her skills.

### Requirements
The file can contain an arbitrary number of rows.
Each row contains such fields in the following order:

- email
- lastname
- firstname
- fiscal code
- description
- last access date

NOTE:

- There cannot be more users with the same email in the DB
- All fields (with the exception of email) can be `null`
- Any file is the new master

### Scenario

- Given a new file to import, when I have imported the file, then the db state has to be updated
- Given an existing user, when the new imported file contains the same email, then the existing user data has to be updated
- Given a new file to import, when I upload a *.txt file, then I should receive an error
- Given a new file to import, when I upload a file that exceeds the limit (in MB), then I should receive an error

### Expectations

The expectations are:

- Modelling needed entities
- Handling errors
- Saving the file everywhere you prefer (Keep in mind the storage provider could change)
- Highlighting complexities you have to cover
- Being able to import a file containing 10k rows
- Writing tests!
