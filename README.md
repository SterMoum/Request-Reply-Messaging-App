# How to Run

## Server Side
The server should be executed this way
> java Server < port number >
>
Port number declares the port that the server listens to

## Client Side
The Client side should be executed this way
>java Client < ip > < port number > < fn_id > < args >

όπου
● <b>ip</b>: Server Ip (use localhost for easy use)
● <b>port number:</b> The port that the server listens to  
● <b> fn_id: </b> The identifier of the executed operation
● <b> args: </b>  The operations' parameters
The operations are the following:


### Create Account (FN_ID: 1)

> java Client < ip > < port number> 1 < username >

Creates an account for the user with the given username.
The function returns a <b>unique code (token)  </b> which is used to
authenticate the user in his next requests.

### Show Accounts (FN_ID: 2)
> java Client < ip > < port number> 2 < authtoken >

A list of all the existed accounts are printed

The file explorer is accessible using the button in left corner of the navigation bar. You can create a new file by clicking the **New file** button in the file explorer. You can also create folders by clicking the **New folder** button.

### Send Message (FN_ID: 3)
> java Client < ip > < port number> 3 < authtoken > < recipient > < message_body >

A message ( <b> < message_body > </b> ) is sent to the user <b> < recipient > </b>


###  Show Inbox (FN_ID: 4)
> java Client < ip > < port number> 4 < authtoken >

It displays a list with all the received messages of the user with the token <b>< authtoken > </b>



### ReadMessage (FN_ID: 5)
> java Client < ip > < port number> 5 < authtoken > < message_id >

This operation displays the content of a message of the user with id
<message_id>. The message is then marked as read


### DeleteMessage (FN_ID: 6)
> java Client < ip > < port number> 6 < authtoken > < message_id >

This operation deletes the message with id < message_id >

