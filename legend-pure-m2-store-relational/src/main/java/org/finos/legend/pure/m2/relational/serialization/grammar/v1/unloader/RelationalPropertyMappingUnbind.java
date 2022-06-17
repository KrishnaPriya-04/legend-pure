// Copyright 2021 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.pure.m2.relational.serialization.grammar.v1.unloader;

import org.finos.legend.pure.m3.compiler.postprocessing.processor.milestoning.MilestoningFunctions;
import org.finos.legend.pure.m3.compiler.unload.unbind.Shared;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.PropertyMapping;
import org.finos.legend.pure.m3.coreinstance.meta.pure.mapping.PropertyMappingsImplementation;
import org.finos.legend.pure.m3.coreinstance.meta.pure.metamodel._import.ImportStub;
import org.finos.legend.pure.m3.coreinstance.meta.pure.tools.GrammarInfoStub;
import org.finos.legend.pure.m3.coreinstance.meta.external.store.relational.mapping.EmbeddedRelationalInstanceSetImplementation;
import org.finos.legend.pure.m3.coreinstance.meta.external.store.relational.mapping.OtherwiseEmbeddedRelationalInstanceSetImplementation;
import org.finos.legend.pure.m3.coreinstance.meta.external.store.relational.mapping.RelationalInstanceSetImplementation;
import org.finos.legend.pure.m3.coreinstance.meta.external.store.relational.mapping.RelationalPropertyMapping;
import org.finos.legend.pure.m3.coreinstance.meta.external.store.relational.metamodel.RelationalOperationElement;
import org.finos.legend.pure.m3.navigation.ProcessorSupport;
import org.finos.legend.pure.m4.coreinstance.CoreInstance;
import org.finos.legend.pure.m4.ModelRepository;
import org.finos.legend.pure.m4.exception.PureCompilationException;

public class RelationalPropertyMappingUnbind
{
    public static void cleanPropertyMappings(PropertyMappingsImplementation relationalInstanceSetImplementation, ModelRepository repository, ProcessorSupport processorSupport, ImportStub mappingImportStub) throws PureCompilationException
    {
        for (PropertyMapping propertyMapping : relationalInstanceSetImplementation._propertyMappings())
        {
            if (MilestoningFunctions.isAutoGeneratedMilestoningNamedDateProperty(propertyMapping._property(), processorSupport))
            {
                MilestoningPropertyMappingUnbind.unbindMilestoningPropertyMapping((RelationalInstanceSetImplementation)relationalInstanceSetImplementation, processorSupport);
            }
            else if (propertyMapping instanceof EmbeddedRelationalInstanceSetImplementation)
            {
                EmbeddedRelationalInstanceSetImplementation embeddedRelationalInstanceSetImplementation = (EmbeddedRelationalInstanceSetImplementation)propertyMapping;
                embeddedRelationalInstanceSetImplementation._sourceSetImplementationIdRemove();
                cleanPropertyMappings(embeddedRelationalInstanceSetImplementation, repository, processorSupport, mappingImportStub);
                CoreInstance _class = embeddedRelationalInstanceSetImplementation._classCoreInstance();
                if (_class != null)
                {
                    Shared.cleanUpReferenceUsage(_class, embeddedRelationalInstanceSetImplementation, processorSupport);
                }
                embeddedRelationalInstanceSetImplementation._classRemove();
                for (RelationalOperationElement pk : embeddedRelationalInstanceSetImplementation._primaryKey())
                {
                    RelationalOperationElementUnbind.cleanNode(pk, repository, processorSupport);
                }
                if (embeddedRelationalInstanceSetImplementation instanceof OtherwiseEmbeddedRelationalInstanceSetImplementation)
                {
                    PropertyMapping otherwiseMapping = ((OtherwiseEmbeddedRelationalInstanceSetImplementation)embeddedRelationalInstanceSetImplementation)._otherwisePropertyMapping();
                    otherwiseMapping._sourceSetImplementationIdRemove();
                    RelationalOperationElement val = otherwiseMapping instanceof RelationalPropertyMapping ? ((RelationalPropertyMapping)otherwiseMapping)._relationalOperationElement() : null;
                    RelationalOperationElementUnbind.cleanNode(val, repository, processorSupport);
                }
            }
            else
            {
                GrammarInfoStub transformer = propertyMapping instanceof RelationalPropertyMapping ? (GrammarInfoStub)((RelationalPropertyMapping)propertyMapping)._transformerCoreInstance() : null;
                // TODO figure out why we sometimes unbind twice (which is why we check if this has already been unbound and is a string)
                if (transformer != null && transformer._original() != null)
                {
                    transformer._value(transformer._original());
                    transformer._originalRemove();
                }
                RelationalOperationElement val = propertyMapping instanceof RelationalPropertyMapping ? ((RelationalPropertyMapping)propertyMapping)._relationalOperationElement() : null;
                RelationalOperationElementUnbind.cleanNode(val, repository, processorSupport);
            }
        }
    }
}
