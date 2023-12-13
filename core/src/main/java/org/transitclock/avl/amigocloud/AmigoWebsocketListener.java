/* (C)2023 */
package org.transitclock.avl.amigocloud;

public interface AmigoWebsocketListener {
    public void onMessage(String message);

    /**
     * Called by WebSocketClient.onClose() to indicate connection closed and there is a problem.
     *
     * @param code
     * @param reason
     */
    public void onClose(int code, String reason);
}
