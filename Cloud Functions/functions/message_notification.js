exports.handler = (event) =>{

	const admin = require('firebase-admin');

	const receiverId = event.params.receiver_id;
	const notificationId = event.params.notification_id;

	console.log('send to: ',receiverId);

	const fromUser = admin.database().ref(`/Notifications/Message_Notifications/${receiverId}/${notificationId}`).once('value');
	return fromUser.then(result =>{

			const from_user_id = result.val().from;

			const messageRef = admin.database().ref(`/Messages/${from_user_id}/${receiverId}/${notificationId}/message`).once('value');
		    const deviceToken = admin.database().ref(`/Users/${receiverId}/device_token`).once('value');
		    const userQuery  = admin.database().ref(`Users/${from_user_id}`).once('value');

		    return Promise.all([messageRef,deviceToken,userQuery]).then(response =>{

		        const message = response[0].val();
		        const token = response[1].val();
		        const sender_name = response[2].val().name;
		        const sender_image = response[2].val().thumbnail;
		        console.log("Msssage Text :",message);
		        console.log("Msssage from :",sender_name);
		        console.log("Msssage image :",sender_image);

		        const payload = {
		       	
						"data":{
							"title_notif" :`${sender_name}`,
							"body_notif":`${message}`,
							"icon_notif":`${sender_image}`,
							"from_user_id":`${from_user_id}`,
							"user_image":`${sender_image}`,
							"type":"text"
							}

		        };
		        return admin.messaging().sendToDevice(token,payload).then(respond =>{
						console.log("Message Sent");
					});
		      });



	});
	
};