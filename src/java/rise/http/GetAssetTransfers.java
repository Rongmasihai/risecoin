package rise.http;

import rise.Account;
import rise.Asset;
import rise.AssetTransfer;
import rise.RiseException;
import rise.db.DbIterator;
import rise.db.DbUtils;
import rise.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAssetTransfers extends APIServlet.APIRequestHandler {

    static final GetAssetTransfers instance = new GetAssetTransfers();

    private GetAssetTransfers() {
        super(new APITag[] {APITag.AE}, "asset", "account", "firstIndex", "lastIndex", "includeAssetInfo");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws RiseException {

        String assetId = Convert.emptyToNull(req.getParameter("asset"));
        String accountId = Convert.emptyToNull(req.getParameter("account"));

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeAssetInfo = !"false".equalsIgnoreCase(req.getParameter("includeAssetInfo"));

        JSONObject response = new JSONObject();
        JSONArray transfersData = new JSONArray();
        DbIterator<AssetTransfer> transfers = null;
        try {
            if (accountId == null) {
                Asset asset = ParameterParser.getAsset(req);
                transfers = asset.getAssetTransfers(firstIndex, lastIndex);
            } else if (assetId == null) {
                Account account = ParameterParser.getAccount(req);
                transfers = account.getAssetTransfers(firstIndex, lastIndex);
            } else {
                Asset asset = ParameterParser.getAsset(req);
                Account account = ParameterParser.getAccount(req);
                transfers = AssetTransfer.getAccountAssetTransfers(account.getId(), asset.getId(), firstIndex, lastIndex);
            }
            while (transfers.hasNext()) {
                transfersData.add(JSONData.assetTransfer(transfers.next(), includeAssetInfo));
            }
        } finally {
            DbUtils.close(transfers);
        }
        response.put("transfers", transfersData);

        return response;
    }

    @Override
    boolean startDbTransaction() {
        return true;
    }
}
