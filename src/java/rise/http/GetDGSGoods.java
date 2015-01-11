package rise.http;

import rise.DigitalGoodsStore;
import rise.RiseException;
import rise.db.DbIterator;
import rise.db.DbUtils;
import rise.db.FilteringIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDGSGoods extends APIServlet.APIRequestHandler {

    static final GetDGSGoods instance = new GetDGSGoods();

    private GetDGSGoods() {
        super(new APITag[] {APITag.DGS}, "seller", "firstIndex", "lastIndex", "inStockOnly", "hideDelisted", "includeCounts");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws RiseException {
        long sellerId = ParameterParser.getSellerId(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean inStockOnly = !"false".equalsIgnoreCase(req.getParameter("inStockOnly"));
        boolean hideDelisted = "true".equalsIgnoreCase(req.getParameter("hideDelisted"));
        boolean includeCounts = !"false".equalsIgnoreCase(req.getParameter("includeCounts"));

        JSONObject response = new JSONObject();
        JSONArray goodsJSON = new JSONArray();
        response.put("goods", goodsJSON);

        FilteringIterator.Filter<DigitalGoodsStore.Goods> filter = hideDelisted ?
                new FilteringIterator.Filter<DigitalGoodsStore.Goods>() {
                    @Override
                    public boolean ok(DigitalGoodsStore.Goods goods) {
                        return ! goods.isDelisted();
                    }
                } :
                new FilteringIterator.Filter<DigitalGoodsStore.Goods>() {
                    @Override
                    public boolean ok(DigitalGoodsStore.Goods goods) {
                        return true;
                    }
                };

        FilteringIterator<DigitalGoodsStore.Goods> iterator = null;
        try {
            DbIterator<DigitalGoodsStore.Goods> goods;
            if (sellerId == 0) {
                if (inStockOnly) {
                    goods = DigitalGoodsStore.getGoodsInStock(0, -1);
                } else {
                    goods = DigitalGoodsStore.getAllGoods(0, -1);
                }
            } else {
                goods = DigitalGoodsStore.getSellerGoods(sellerId, inStockOnly, 0, -1);
            }
            iterator = new FilteringIterator<>(goods, filter, firstIndex, lastIndex);
            while (iterator.hasNext()) {
                DigitalGoodsStore.Goods good = iterator.next();
                goodsJSON.add(JSONData.goods(good, includeCounts));
            }
        } finally {
            DbUtils.close(iterator);
        }

        return response;
    }

}
