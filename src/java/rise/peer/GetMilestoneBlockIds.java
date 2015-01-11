package rise.peer;

import rise.Block;
import rise.Rise;
import rise.util.Convert;
import rise.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

final class GetMilestoneBlockIds extends PeerServlet.PeerRequestHandler {

    static final GetMilestoneBlockIds instance = new GetMilestoneBlockIds();

    private GetMilestoneBlockIds() {}


    @Override
    JSONStreamAware processRequest(JSONObject request, Peer peer) {

        JSONObject response = new JSONObject();
        try {

            JSONArray milestoneBlockIds = new JSONArray();

            String lastBlockIdString = (String) request.get("lastBlockId");
            if (lastBlockIdString != null) {
                long lastBlockId = Convert.parseUnsignedLong(lastBlockIdString);
                long myLastBlockId = Rise.getBlockchain().getLastBlock().getId();
                if (myLastBlockId == lastBlockId || Rise.getBlockchain().hasBlock(lastBlockId)) {
                    milestoneBlockIds.add(lastBlockIdString);
                    response.put("milestoneBlockIds", milestoneBlockIds);
                    if (myLastBlockId == lastBlockId) {
                        response.put("last", Boolean.TRUE);
                    }
                    return response;
                }
            }

            long blockId;
            int height;
            int jump;
            int limit = 10;
            int blockchainHeight = Rise.getBlockchain().getHeight();
            String lastMilestoneBlockIdString = (String) request.get("lastMilestoneBlockId");
            if (lastMilestoneBlockIdString != null) {
                Block lastMilestoneBlock = Rise.getBlockchain().getBlock(Convert.parseUnsignedLong(lastMilestoneBlockIdString));
                if (lastMilestoneBlock == null) {
                    throw new IllegalStateException("Don't have block " + lastMilestoneBlockIdString);
                }
                height = lastMilestoneBlock.getHeight();
                jump = Math.min(1440, Math.max(blockchainHeight - height, 1));
                height = Math.max(height - jump, 0);
            } else if (lastBlockIdString != null) {
                height = blockchainHeight;
                jump = 10;
            } else {
                peer.blacklist();
                response.put("error", "Old getMilestoneBlockIds protocol not supported, please upgrade");
                return response;
            }
            blockId = Rise.getBlockchain().getBlockIdAtHeight(height);

            while (height > 0 && limit-- > 0) {
                milestoneBlockIds.add(Convert.toUnsignedLong(blockId));
                blockId = Rise.getBlockchain().getBlockIdAtHeight(height);
                height = height - jump;
            }
            response.put("milestoneBlockIds", milestoneBlockIds);

        } catch (RuntimeException e) {
            Logger.logDebugMessage(e.toString());
            response.put("error", e.toString());
        }

        return response;
    }

}
