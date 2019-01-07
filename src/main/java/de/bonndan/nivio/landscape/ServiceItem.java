package de.bonndan.nivio.landscape;

import java.util.Set;

public interface ServiceItem {

    String LAYER_INFRASTRUCTURE = "infrastructure";
    String LAYER_APPLICATION = "applications";
    String LAYER_INGRESS = "ingress";

    String TYPE_SERVICE = "service";

    String IDENTIFIER_VALIDATION = "^[a-z0-9\\.\\:_-]{3,256}$";


    /**
     * Returns the landscape-wide unique identifier of a server or application.
     *
     */
    String getIdentifier();

    /**
     * @return the fqi to identify the landscape item
     */
    FullyQualifiedIdentifier getFullyQualifiedIdentifier();

    /**
     * @return the type (ingress, service, infrastructure)
     */
    String getLayer();

    /**
     * @return the type (service, database, queue, loadbalancer...)
     */
    String getType();

    String getName();
    String getShort_name();

    String getGroup();

    String getSoftware();

    String getVersion();

    String getHomepage();

    String getRepository();

    String getContact();

    String getTeam();

    String getVisibility();

    void setStatus(StatusItem statusItem);

    Set<StatusItem> getStatuses();

    String getMachine();

    String getScale();

    String getHost_type();

    Set<String> getNetworks();

    String getDescription();

    String[] getTags();

    String getNote();

    String getOwner();

    Set<InterfaceItem> getInterfaces();

    Set<DataFlowItem> getDataFlow();


}