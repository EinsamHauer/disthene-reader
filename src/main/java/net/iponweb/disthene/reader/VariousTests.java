package net.iponweb.disthene.reader;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Andrei Ivanov
 */
public class VariousTests {
    public static void main(String[] args) throws Exception {
        System.out.println(StringUtils.countMatches("userverlua*.userver.requests.path.*_bid.count", "."));
    }

/*
        public static void main(String[] args) throws Exception {

//        String regEx = "userverlua.*\\.userver\\.requests\\.path\\..*_bid.count";
        String regEx = "[^\\.]*";
        String tenant = "bidswitch";

        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", "cyanite").build();
        TransportClient client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress("es5.devops.iponweb.net", 9300))
                .addTransportAddress(new InetSocketTransportAddress("es6.devops.iponweb.net", 9300))
                .addTransportAddress(new InetSocketTransportAddress("es7.devops.iponweb.net", 9300))
                .addTransportAddress(new InetSocketTransportAddress("es8.devops.iponweb.net", 9300));

        long start = System.nanoTime();
        SearchResponse response = client.prepareSearch("cyanite_paths")
                .setScroll(new TimeValue(120000))
                .setSize(10000)
//                .addField("path")
                .setQuery(QueryBuilders.filteredQuery(QueryBuilders.regexpQuery("path", regEx),
                        FilterBuilders.termFilter("tenant", tenant)))
                .execute().actionGet();

        while (response.getHits().getHits().length > 0) {
            for (SearchHit hit : response.getHits()) {
                System.out.println(hit.getSourceAsString());
            }
            System.out.println("Got portion");
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(new TimeValue(120000))
                    .execute().actionGet();

        }
        long end = System.nanoTime();
        System.out.println("Fetched paths from ES in " + (end - start) / 1000000 + "ms");

    }

    private static class CyanitePath {
        private String path;
        private int depth;
        private String tenant;
        private boolean leaf;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public String getTenant() {
            return tenant;
        }

        public void setTenant(String tenant) {
            this.tenant = tenant;
        }

        public boolean isLeaf() {
            return leaf;
        }

        public void setLeaf(boolean leaf) {
            this.leaf = leaf;
        }
    }
*/
}
