/**
 * This file is part of NIF transfer library for the General Entity Annotator Benchmark.
 *
 * NIF transfer library for the General Entity Annotator Benchmark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NIF transfer library for the General Entity Annotator Benchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NIF transfer library for the General Entity Annotator Benchmark.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.aksw.gerbil.io.nif.DocumentListWriter;
import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.SpanImpl;
import org.junit.Ignore;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import IOContent.TextReader;
import IOContent.TextWriter;

/**
 * Diese Klasse generiert NIF files aus Texten welche ein Wikimardown für Url's haben.
 * Basierend auf einem Beispiel von Michael Röder (roeder@informatik.uni-leipzig.de)
 * @author TTurke
 *
 */
@Ignore
class AnnotedTextToNIFConverter 
{
	
	/**
	 * This method create a text marking as type SpanImpl.
	 * That means its just the marking and no additional information about appending uris.
	 * @param defObj
	 * @return SpanImpl marking
	 */
	public static SpanImpl createMarkingSpanImpl(DefinitionObject defObj)
	{
		if(defObj.getStartPos() > -1 && defObj.getContent().length() > 0)
		{
			return new SpanImpl(defObj.getStartPos(), defObj.getContent().length());
		}
		
		return null;
		
	}
	
	/**
	 * This method create a text marking as type NamedEntity.
	 * That means there are additional information about appending uris.
	 * @param defObj
	 * @return NamedEntity marking
	 */
	public static NamedEntity createMarkingNamedEntity (DefinitionObject defObj)
	{
		if(defObj.getUrlAmount() > 0)
		{
			return new NamedEntity(defObj.getStartPos(), defObj.getContent().length(), defObj.getEngWikiUrls());
		}
		
		return null;
	}
	
	
	/**
	 * This method calculate and create a NIF file for a given Text with Wikipedia Markup annotations
	 * @param path
	 */
	public static String doTheMagic(String path)
	{
		String input = TextReader.fileReader(path);
		ArrayList<DefinitionObject> DOList = GatherAnnotationInformations.getAnnotationDefs(input);
		Document document = new DocumentImpl(input, "http://example.org/test_document");
		
		for(DefinitionObject dobj : DOList)
		{		
			if(dobj.getUrlAmount() > 0)
			{
				document.addMarking(createMarkingNamedEntity(dobj));
			}else{
				document.addMarking(createMarkingSpanImpl(dobj));
			}
			
		}

		List<Document> documents = new ArrayList<Document>();
		documents.add(document);

		// Writing our new list of documents to a String
		NIFWriter writer = new TurtleNIFWriter();
		String nifString = writer.writeNIF(documents);

		//Content not necessary for my work
		
//		// After generating a NIF corpus, it can be helpful to parse the NIF using a `NIFParser` instance.
//		NIFParser parser = new TurtleNIFParser();
//		parser.parseNIF(nifString);
//
//		// Instead of text containing the NIF information, a jena RDF `Model` can be created.
//		DocumentListWriter listWriter = new DocumentListWriter();
//		Model nifModel = ModelFactory.createDefaultModel();
//		listWriter.writeDocumentsToModel(nifModel, documents);
		
		return nifString;
	}
	

	/**
	 * This method starts the program workflow/magic
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException 
	{
		//For static search of just 1 File
		String static_path = "";
		String defaultName = "default-out.xml";
		int iter = 1;
		
		if(args.length > 0)
		{
			for(String cur : args)
			{
				try 
				{	
					System.out.println(iter);
					TextWriter.writeToProgramFolder(defaultName+"."+iter, doTheMagic(cur));
					iter++;
				} catch (IllegalArgumentException iae) { iae.printStackTrace(); }
			}
			
		}else{
			if(static_path.length() > 0)
			{
				try 
				{
					TextWriter.writeToProgramFolder(defaultName, doTheMagic(static_path));
				} catch (IllegalArgumentException iae) { iae.printStackTrace(); }

			}else{
				System.err.println("No Path given! Program terminate!");
			}
		}
		
	}
}