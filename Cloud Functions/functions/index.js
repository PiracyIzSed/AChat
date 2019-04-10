'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const seenTrigger = require('./seen_trigger');
const reqNotif = require('./request_notification');
const messageNotif = require('./message_notification');

admin.initializeApp(functions.config().firebase);

exports.updateSeen = functions.database.ref('/Chat/{receiver_id}/{sender_id}/online').onUpdate(seenTrigger.handler);
exports.requestNotification = functions.database.ref('/Notifications/Request_Notifications/{receiver_id}/{notification_id}').onWrite(reqNotif.handler);
exports.messageNotification = functions.database.ref('/Notifications/Message_Notifications/{receiver_id}/{notification_id}').onWrite(messageNotif.handler);