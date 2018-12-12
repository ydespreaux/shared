package com.ydespreaux.shared.data.elasticsearch.utils;

import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.IndexTemplateMetaData;

import java.io.IOException;
import java.util.List;

public final class AdminClientUtils {

    /**
     *
     * @param templateName
     * @return
     * @throws IOException
     */
    public static IndexTemplateMetaData getTemplate(RestHighLevelClient client, String templateName) throws IOException {
        GetIndexTemplatesRequest request = new GetIndexTemplatesRequest(templateName);
        GetIndexTemplatesResponse response = client.indices().getTemplate(request, RequestOptions.DEFAULT);
        List<IndexTemplateMetaData> templates = response.getIndexTemplates();
        if (templates.isEmpty()) {
            return null;
        }
        return templates.get(0);
    }

}
