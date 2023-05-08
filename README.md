Here is what the current program does:

Note**: Nonces are generated and checked for every message

1. Creating a user
	- User enters username and password they want to use
	- passwd.json inside KDCServer folder is updated
	- User receives Base32 Key to get a valid OTP 
	- Program ends
2. Authenticating a user
	- User enters username, password, and OTP
	- User is authenticated 
	- Session key is handed to client and user is connected to cloud server
	- Handshake with client and cloud is performed
	- Cloud server asks user what they want to do: 1 - Send a file OR 2 - Request a file
		AT THIS POINT EVERYTHING IS ENCRYPTED WITH THE SESSION KEY AND TLS ON TOP
		- Upon User File Send:
			- User enters a valid path configuration they want to share
			- User enters an associated file password
			- A key is derived from the users file password and username (username acts as a salt for scrypt)
			- User enters the key words the want to associate with that file
				- These are stored in an arraylist but sent as a string
			- Key words and nonce are encrypted with session key THEN sent off to cloud
		- Upon Cloud Send Receive:
			- Cloud decrypts string arraylist and nonce, printed out to screen to verify

		- Upon User File Request:
			- User enters keywords they want to use to search
			- Nonce is generated and encrpyted alongside the ArrayList<String> of keywords (Just like in user send)
		- Upon Cloud File Request:
			- Cloud decrypts string arraylist and nonce, printed out to screen to verify

To run:
Build the classes with NetBeans
Then the client and servers can be started with the batch files located in the root directory
Use FreeOTP to generate a QR code from the base 32 key. Ensure that it is set to SHA1 timeout with 6 digits.
