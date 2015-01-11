package rise.peer;

import rise.Rise;
import rise.RiseException;
import rise.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

final class ProcessBlock extends PeerServlet.PeerRequestHandler {

    static final ProcessBlock instance = new ProcessBlock();

    private ProcessBlock() {}

    private static final JSONStreamAware ACCEPTED;
    static {
        JSONObject response = new JSONObject();
        response.put("accepted", true);
        ACCEPTED = JSON.prepare(response);
    }

    private static final JSONStreamAware NOT_ACCEPTED;
    static {
        JSONObject response = new JSONObject();
        response.put("accepted", false);
        NOT_ACCEPTED = JSON.prepare(response);
    }

    @Override
    JSONStreamAware processRequest(JSONObject request, Peer peer) {

        try {

            if (! Rise.getBlockchain().getLastBlock().getStringId().equals(request.get("previousBlock"))) {
                // do this check first to avoid validation failures of future blocks and transactions
                // when loading blockchain from scratch
                return NOT_ACCEPTED;
            }
            Rise.getBlockchainProcessor().processPeerBlock(request);
            return ACCEPTED;

        } catch (RiseException|RuntimeException e) {
            if (peer != null) {
                peer.blacklist(e);
            }
            return NOT_ACCEPTED;
        }

    }

}
