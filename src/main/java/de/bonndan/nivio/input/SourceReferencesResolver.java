package de.bonndan.nivio.input;

import de.bonndan.nivio.ProcessingException;
import de.bonndan.nivio.input.dto.DataFlowDescription;
import de.bonndan.nivio.input.dto.LandscapeDescription;
import de.bonndan.nivio.input.dto.ItemDescription;
import de.bonndan.nivio.model.LandscapeItem;
import de.bonndan.nivio.model.Items;

import java.util.ArrayList;
import java.util.List;

import static de.bonndan.nivio.model.Items.find;

public class SourceReferencesResolver {

    public void resolve(final LandscapeDescription env, final ProcessLog log) {

        env.getSourceReferences().forEach(ref -> {
            try {
                ItemDescriptionFactory factory = ItemDescriptionFormatFactory.getFactory(ref, env);

                List<ItemDescription> descriptions = factory.getDescriptions(ref);

                ref.getAssignTemplates().forEach((key, value) -> {

                    LandscapeItem template = find(key, "", env.getTemplates()).orElse(null);
                    if (template == null) {
                        log.warn("Could not find template to assign: " + key);
                        return;
                    }

                    value.forEach(identifier -> {
                        Items.filter(identifier, descriptions)
                                .forEach(item -> ItemDescriptionFactory.assignTemplateValues((ItemDescription) item, (ItemDescription) template));
                    });

                });

                env.addItems(descriptions);
            } catch (ProcessingException ex) {
                log.warn("Failed to resolve source reference " + ref, ex);
                env.setIsPartial(true);
            }
        });

        resolveTemplateQueries(env.getItemDescriptions());
    }

    /**
     * Finds providers or data flow targets named in queries.
     */
    private void resolveTemplateQueries(final List<ItemDescription> itemDescriptions) {
        itemDescriptions.forEach(description -> {

            //provider
            List<String> providedBy = description.getProvidedBy();
            description.setProvidedBy(new ArrayList<>());
            providedBy.forEach(condition -> {
                Items.filter(condition, itemDescriptions)
                        .forEach(result -> description.getProvidedBy().add(result.getIdentifier()));
            });

            description.getDataFlow().forEach(dataFlowItem -> {
                Items.filter(dataFlowItem.getTarget(), itemDescriptions).stream()
                        .findFirst()
                        .ifPresent(service -> ((DataFlowDescription)dataFlowItem).setTarget(service.getFullyQualifiedIdentifier().toString()));
            });
        });
    }

}
