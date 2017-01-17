/*
 * Copyright (c) 2010 - 2015 Norwegian Agency for Pupblic Government and eGovernment (Difi)
 *
 * This file is part of Oxalis.
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission
 * - subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the Licence
 *  is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 */

package eu.peppol.document;

import eu.peppol.PeppolStandardBusinessHeader;
import no.difi.vefa.peppol.sbdh.SbdWriter;
import no.difi.vefa.peppol.sbdh.util.XMLStreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Takes a document and wraps it together with headers into a StandardBusinessDocument.
 *
 * The SBDH part of the document is constructed from the headers.
 * The document will be the payload (xs:any) following the SBDH.
 *
 * @author thore
 * @author steinar
 */
public class SbdhWrapper {

    /**
     * Wraps payload + headers into a StandardBusinessDocument
     * @param inputStream the input stream to be wrapped
     * @param headers the headers to use for sbdh
     * @return byte buffer with the resulting output in utf-8
     */
    public byte[] wrap(InputStream inputStream, PeppolStandardBusinessHeader headers) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (SbdWriter sbdWriter = SbdWriter.newInstance(baos, headers.toVefa())) {
            XMLStreamUtils.copy(inputStream, sbdWriter.xmlWriter());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to wrap document inside SBD (SBDH). " + ex.getMessage(), ex);
        }

        return baos.toByteArray();
    }
}
