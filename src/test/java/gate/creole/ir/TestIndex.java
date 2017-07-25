/*
 * TestIndex.java
 *
 * Copyright (c) 1995-2012, The University of Sheffield. See the file
 * COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 2, June 1991
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 *
 * Rosen Marinov, 19/Apr/2002
 *
 */

package gate.creole.ir;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.junit.Assert;

import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.creole.ir.lucene.LuceneSearch;
import gate.test.GATEPluginTestCase;
import gate.util.GateException;

public class TestIndex extends GATEPluginTestCase {

  private static String TEMP_LOCATION = null;

  private Corpus corpus = null;

  private DataStore sds = null;

  /** Fixture set up */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    try {

      File storageDir = File.createTempFile("TestIndex__", "__StorageDir");

      if(null == TEMP_LOCATION) {
        File indexDir = File.createTempFile("LuceneIndex__", "__Dir");
        TEMP_LOCATION = indexDir.getAbsolutePath();
      }

      // System.out.println("temp=["+TEMP_LOCATION+"]");
      // System.out.println("temp2=["+indexDir.getAbsoluteFile()+"]");

      storageDir.delete();
      // create and open a serial data store
      sds = Factory.createDataStore("gate.persist.SerialDataStore",
          storageDir.toURI().toURL().toString());

      sds.open();

      URL docURL = TestIndex.class.getResource("/gate/resources/gate.ac.uk/tests/doc0.html");

      Document doc0 = Factory.newDocument(docURL);
      doc0.getFeatures().put("author", "John Smith");

      Corpus corp = Factory.newCorpus("LuceneTestCorpus");
      corp.add(doc0);
      corpus = (Corpus)sds.adopt(corp);
      sds.sync(corpus);

    } catch(Exception e) {
      e.printStackTrace();
      throw new GateException(e.getMessage());
    }
  } // setUp

  /**
   * Put things back as they should be after running tests (reinitialise the
   * CREOLE register).
   */
  @Override
  public void tearDown() throws Exception {
    sds.delete();
  } // tearDown

  /** Create new index. */
  public void testIndex_01() throws IndexException {
    IndexedCorpus ic = (IndexedCorpus)corpus;
    DefaultIndexDefinition did = new DefaultIndexDefinition();
    did.setIrEngineClassName(
        gate.creole.ir.lucene.LuceneIREngine.class.getName());

    // did.setIndexType(GateConstants.IR_LUCENE_INVFILE);

    did.setIndexLocation(TEMP_LOCATION);
    did.addIndexField(
        new IndexField("content", new DocumentContentReader(), false));
    did.addIndexField(new IndexField("author", null, false));

    ic.setIndexDefinition(did);

    ic.getIndexManager().deleteIndex();
    ic.getIndexManager().createIndex();

  }

  /** Optimize existing index. */
  public void testIndex_02() throws IndexException {
    IndexedCorpus ic = (IndexedCorpus)corpus;
    DefaultIndexDefinition did = new DefaultIndexDefinition();
    // did.setIndexType(GateConstants.IR_LUCENE_INVFILE);
    did.setIrEngineClassName(
        gate.creole.ir.lucene.LuceneIREngine.class.getName());

    did.setIndexLocation(TEMP_LOCATION);

    ic.setIndexDefinition(did);

    ic.getIndexManager().optimizeIndex();
  }

  /** Search in existing index. */
  public void testIndex_10() throws IndexException, SearchException {
    IndexedCorpus ic = (IndexedCorpus)corpus;
    DefaultIndexDefinition did = new DefaultIndexDefinition();
    // did.setIndexType(GateConstants.IR_LUCENE_INVFILE);
    did.setIrEngineClassName(
        gate.creole.ir.lucene.LuceneIREngine.class.getName());

    did.setIndexLocation(TEMP_LOCATION);

    ic.setIndexDefinition(did);

    Search search = new LuceneSearch();
    search.setCorpus(ic);

    QueryResultList res = search.search("+content:Diller +author:John", 10);

    Iterator<QueryResult> it = res.getQueryResults();

    // while (it.hasNext()) {
    // QueryResult qr = (QueryResult) it.next();
    // System.out.println("DOCUMENT_ID="+ qr.getDocumentID() +",
    // scrore="+qr.getScore());
    // }
    Assert.assertTrue(it.hasNext());
  }

  /** Delete index. */
  public void testIndex_101() throws IndexException {
    IndexedCorpus ic = (IndexedCorpus)corpus;
    DefaultIndexDefinition did = new DefaultIndexDefinition();
    // did.setIndexType(GateConstants.IR_LUCENE_INVFILE);
    did.setIrEngineClassName(
        gate.creole.ir.lucene.LuceneIREngine.class.getName());

    did.setIndexLocation(TEMP_LOCATION);

    ic.setIndexDefinition(did);

    ic.getIndexManager().deleteIndex();
  }
}
