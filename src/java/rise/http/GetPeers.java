package rise.http;

import rise.peer.Peer;
import rise.peer.Peers;
import rise.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetPeers extends APIServlet.APIRequestHandler {

    static final GetPeers instance = new GetPeers();

    private GetPeers() {
        super(new APITag[] {APITag.INFO}, "active", "state");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {

        boolean active = "true".equalsIgnoreCase(req.getParameter("active"));
        String stateValue = Convert.emptyToNull(req.getParameter("state"));

        JSONArray peers = new JSONArray();
        for (Peer peer : active ? Peers.getActivePeers() : stateValue != null ? Peers.getPeers(Peer.State.valueOf(stateValue)) : Peers.getAllPeers()) {
            peers.add(peer.getPeerAddress());
        }

        JSONObject response = new JSONObject();
        response.put("peers", peers);
        return response;
    }

}
