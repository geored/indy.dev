package org.commonjava.indy.promote.rules

import org.apache.commons.lang.StringUtils
import org.commonjava.indy.promote.validate.model.ValidationRequest
import org.commonjava.indy.promote.validate.model.ValidationRule
import org.commonjava.maven.galley.maven.rel.ModelProcessorConfig

class MavenNoVersionRanges implements ValidationRule {

    String validate(ValidationRequest request) {
        def verifyStoreKeys = request.getTools().getValidationStoreKeys(request, true)

        def errors = Collections.synchronizedList(new ArrayList());
        def tools = request.getTools()
        def dc = new ModelProcessorConfig().setIncludeBuildSection(true).setIncludeManagedPlugins(true).setIncludeManagedDependencies(true)

        tools.paralleledEach(request.getSourcePaths(), { it ->
            if (it.endsWith(".pom")) {
                def relationships = tools.getRelationshipsForPom(it, dc, request, verifyStoreKeys)
                if (relationships != null) {
                    tools.forEach(relationships, { rel ->
                        def target = rel.getTarget()
                        if (!target.getVersionSpec().isSingle()) {
                            errors.add(String.format("%s uses a compound version in: %s", target, it))
                        }
                    })
                }
            }
        })

        errors.isEmpty() ? null: StringUtils.join(errors, "\n")
    }
}