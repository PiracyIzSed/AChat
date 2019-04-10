exports.handler = (event) =>{

	const admin = require('firebase-admin');

	const receiverId = event.params.receiver_id;
	const notificationId = event.params.notification_id;

	console.log('send to: ',receiverId);


	if(!event.data.val()){
		return console.log('Notification deleted',notification_id);
	}

	const fromUser = admin.database().ref(`/Notifications/Request_Notifications/${receiverId}/${notificationId}`).once('value').then(fromUserResult =>{

		const from_user_id = fromUserResult.val().from;
		console.log("From :",from_user_id);


	    
	    const userQuery  = admin.database().ref(`/Users/${from_user_id}/name`).once('value');
	    const deviceToken = admin.database().ref(`/Users/${receiverId}/device_token`).once('value');

	    return Promise.all([userQuery,deviceToken]).then(result=>{

	    	const userName = result[0].val();
	    	const token_id = result[1].val();

			const payload = {
				
					"data":{
						"title_notif" :"New Friend Request",
						"body_notif":`${userName} wants to be your friend`,
						"icon_notif":'default',
						"from_user_id":`${from_user_id}`,
						"type":"friend_request"
						}
				};
				return admin.messaging().sendToDevice(token_id,payload).then(response =>{
					console.log("Notification Sent");
				});
	        }); 
	     }); 
 
};