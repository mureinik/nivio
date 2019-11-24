package de.bonndan.nivio.input.kubernetes;

import de.bonndan.nivio.input.ItemDescriptionFactory;
import de.bonndan.nivio.input.dto.ItemDescription;
import de.bonndan.nivio.input.dto.RelationDescription;
import de.bonndan.nivio.input.dto.SourceReference;
import de.bonndan.nivio.model.Items;
import de.bonndan.nivio.model.LandscapeItem;
import de.bonndan.nivio.model.RelationBuilder;
import de.bonndan.nivio.util.URLHelper;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@org.springframework.stereotype.Service
public class ItemDescriptionFactoryKubernetes implements ItemDescriptionFactory {

    public static final String NAMESPACE = "namespace";
    public static final String GROUP_LABEL = "groupLabel";

    private String namespace = null;
    private String groupLabel = null;
    private KubernetesClient client;


    public ItemDescriptionFactoryKubernetes() {

    }

    public ItemDescriptionFactoryKubernetes(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public List<String> getFormats() {
        return Arrays.asList("kubernetes", "k8s");
    }

    @Override
    public List<ItemDescription> getDescriptions(SourceReference reference, URL baseUrl) {

        try {
            if (!StringUtils.isEmpty(reference.getUrl())) {
                URL url = new URL(reference.getUrl());
                Map<String, String> params = URLHelper.splitQuery(url);
                if (params.containsKey(NAMESPACE))
                    namespace = params.get(NAMESPACE);
                if (params.containsKey(GROUP_LABEL))
                    this.groupLabel = params.get(GROUP_LABEL);
            }
        } catch (MalformedURLException ignored) {

        }

        KubernetesClient client = getClient(reference.getUrl());

        List<ItemDescription> descriptions = new ArrayList<>();
        client.pods().list().getItems().stream()
                .filter(pod -> {
                    if (namespace == null)
                        return true;
                    return namespace.equals(pod.getMetadata().getNamespace());
                })
                .forEach(pod -> descriptions.addAll(createDescriptionFromService(pod)));

        client.services().list().getItems().stream()
                .filter(service -> {
                    if (namespace == null)
                        return true;
                    return namespace.equals(service.getMetadata().getNamespace());
                })
                .forEach(service -> descriptions.addAll(createDescriptionFromService(service, descriptions)));

        return descriptions;
    }

    private Collection<? extends ItemDescription> createDescriptionFromService(Service kubernetesService, List<ItemDescription> items) {
        List<ItemDescription> descriptions = new ArrayList<>();

        ItemDescription service = new ItemDescription();
        service.setIdentifier(kubernetesService.getMetadata().getName());
        service.setLayer(LandscapeItem.LAYER_INGRESS);
        service.setType(kubernetesService.getSpec().getType());

        String group = getGroup(kubernetesService);
        service.setGroup(group);

        String targetId = "";
        Map<String, String> selector = kubernetesService.getSpec().getSelector();
        if (selector != null)
             targetId = selector.getOrDefault("app", null);
        if (!StringUtils.isEmpty(targetId)) {
            Items.find(targetId, group, items).ifPresent(target -> {
                RelationDescription provides = RelationBuilder.provides(service, target);
                ((ItemDescription) target).addRelation(provides);
                service.addRelation(provides);
            });
        }

        descriptions.add(service);

        return descriptions;
    }

    public Config getConfiguration() {
        return getClient("").getConfiguration();
    }

    private List<ItemDescription> createDescriptionFromService(Pod pod) {

        List<ItemDescription> descriptions = new ArrayList<>();

        String group = getGroup(pod);
        pod.getSpec().getContainers().forEach(container -> {
            ItemDescription description = new ItemDescription();
            description.setGroup(group);
            description.setName(container.getName());
            description.setIdentifier(container.getName());
            description.setSoftware(container.getImage());
            description.setMachine(pod.getSpec().getNodeName()); //ip?
            description.setLabels(pod.getMetadata().getLabels());

            // TODO
            //set Labels, introduce new labels property (docker/k8s)
            //description.getProvided_by().add(pod.getSpec().)
            //description.setScale(...);
            // statuses: pod.getStatus()
            //description.setNetworks();

            descriptions.add(description);
        });

        return descriptions;
    }

    private String getGroup(HasMetadata hasMetadata) {
        if (groupLabel != null)
            return hasMetadata.getMetadata().getLabels().getOrDefault(groupLabel, "");

        return "";
    }

    private KubernetesClient getClient(String context) {
        if (this.client != null)
            return this.client;

        Config config = Config.autoConfigure(context);    //https://github.com/fabric8io/kubernetes-client#configuring-the-client

        this.client = new DefaultKubernetesClient(config);
        return this.client;
    }
}