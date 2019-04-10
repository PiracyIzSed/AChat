exports.handler = (event) =>{

		const senderId = event.params.sender_id;
		const receiverId = event.params.receiver_id;
		const admin = require('firebase-admin');
		const status = event.data.val();

		if(status==="true"){
			admin.database().ref(`/Messages/${receiverId}/${senderId}`).orderByChild('seen').equalTo("false").once('value').then(results =>{
				 results.forEach(messageShot =>{
				 	const messageId = messageShot.key;
					
						const fromId = messageShot.val().from;
						if(fromId===senderId){
							console.log('message id :',messageId,"sender id:",fromId);
							const p1 = admin.database().ref(`/Messages/${senderId}/${receiverId}/${messageId}/seen`).set("true");
							const p2 = admin.database().ref(`/Messages/${receiverId}/${senderId}/${messageId}/seen`).set("true");
							Promise.all([p1,p2]);
						}
				});
			
			}).catch(reason =>{
				console.log(reason);
			});
		}else{
			return console.log('online status of receiver ',receiverId,status);
		}

};