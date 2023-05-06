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

Here's what's missing and needs to be done:
	1. Creating/Authenticating user has one issue. Create updates passwd.json but authn uses passwd.json & secrets.json.
	   I find secrets.json to not be necessary and very insecure since it stores p.t. passwords with usernames. The auth
	   should ONLY check passwd.json. 
	2. Data entered by users during file send are not handled/stored. This can possibly be accomplished in a similar fashion
	   to how passwd.json is setup. The data I am talking about here is: File Password, File Key, and Key Words.
	3. Keywords need to be hashed before encrypted and send.
	4. Once the file path is specified, client needs to encrypt with the file password derived key as well as session key and send it fragmented.
	   How you want to associate the seperately sent keywords with the file is up in the air. They can remain seperate a.k.a one after another 
           or send keywords with file in the same packet.
	5. After setting up sending data, searchable encryption must take place. This means the packet (which is already set up) sent to the server
           during this protocol can be verified. Verfiying means, checking if the the HASHED keywords sent by the user match what the cloud has for some file.
           Once verified, the cloud sends the encrypted file to user in fragments. From here, user needs to decrypt by entering the file password as well as the
           username it belongs to inorder to derive the proper key thereof. Once decrypted, allow user to specify a directory where the wish to save the file.
           At this point, we should be done. 
