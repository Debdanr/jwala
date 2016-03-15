/**
 * The server state web socket service.
 */
var serverStateWebSocketService = {
    connect: function(msgHandler, connectedCallback, errorHandler) {
        var self = this;
        var socket = new SockJS("/toc/endpoint");
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function(frame) {
            console.log("Connected: " + frame);
            stompClient.subscribe("/topic/server-states", function(rawMsg){
                msgHandler(JSON.parse(rawMsg.body));
            });
            connectedCallback(frame);
        }, errorHandler);
    },
    disconnect: function() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
        console.log("Disconnected");
    }
}