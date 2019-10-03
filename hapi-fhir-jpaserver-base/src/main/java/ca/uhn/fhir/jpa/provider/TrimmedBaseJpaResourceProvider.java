package ca.uhn.fhir.jpa.provider;

/*
 * #%L
 * HAPI FHIR JPA Server
 * %%
 * Copyright (C) 2014 - 2019 University Health Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.jpa.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.util.ExpungeOptions;
import ca.uhn.fhir.jpa.util.ExpungeOutcome;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.util.CoverageIgnore;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public abstract class TrimmedBaseJpaResourceProvider<T extends IBaseResource> extends BaseJpaProvider implements IResourceProvider {

    private IFhirResourceDao<T> myDao;


    @CoverageIgnore
    public TrimmedBaseJpaResourceProvider(IFhirResourceDao<T> theDao) {
        myDao = theDao;
    }


    protected Parameters doExpunge(IIdType theIdParam, IPrimitiveType<? extends Integer> theLimit, IPrimitiveType<? extends Boolean> theExpungeDeletedResources, IPrimitiveType<? extends Boolean> theExpungeOldVersions, IPrimitiveType<? extends Boolean> theExpungeEverything, RequestDetails theRequest) {

        ExpungeOptions options = createExpungeOptions(theLimit, theExpungeDeletedResources, theExpungeOldVersions, theExpungeEverything);

        ExpungeOutcome outcome;
        if (theIdParam != null) {
            outcome = getDao().expunge(theIdParam, options, theRequest);
        } else {
            outcome = getDao().expunge(options, theRequest);
        }

        return createExpungeResponse(outcome);
    }

    public IFhirResourceDao<T> getDao() {
        return myDao;
    }

    @Required
    public void setDao(IFhirResourceDao<T> theDao) {
        myDao = theDao;
    }



    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return myDao.getResourceType();
    }



    @Read(version = true)
    public T read(HttpServletRequest theRequest, @IdParam IIdType theId, RequestDetails theRequestDetails) {
        startRequest(theRequest);
        try {
            return myDao.read(theId, theRequestDetails);
        } finally {
            endRequest(theRequest);
        }
    }

    public DateRangeParam processSinceOrAt(Date theSince, DateRangeParam theAt) {
        return super.processSinceOrAt(theSince, theAt);
    }

}
