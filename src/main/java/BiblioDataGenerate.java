
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.*;

public class BiblioDataGenerate {

    public static final String SEPARADOR = ",";
    public static String[][] atributos = new String[3000][17];
    public static int limite = 0;



    public static String[] tipos_Fabio = {"Article", "Erratum", "Review", "ConferencePaper", "Letter", "Editorial"};
    static boolean tipo = false;
    
    public static void main(String[] args) throws FileNotFoundException {

        leerDatos();


        Model model = ModelFactory.createDefaultModel();

        //definicion del fichero donde insertaremos los datos RDF
        File f = new File("src/main/resources/RDF/TecnologiasEmergentes.rdf");
        FileOutputStream os = new FileOutputStream(f);

        //Set prefix for the URI base (new data)
        String dataPrefix = "http://utpl.edu.ec/tecnologiasemergentes/ontology/";
        model.setNsPrefix("myData", dataPrefix);
        Model myOntoModel = ModelFactory.createDefaultModel();

        String dbo = "http://dbpedia.org/ontology/";
        model.setNsPrefix("dbo", dbo);
        Model dboModel = ModelFactory.createDefaultModel();

        String dbr = "http://dbpedia.org/resource/";
        model.setNsPrefix("dbr", dbr);
        Model dbrModel = ModelFactory.createDefaultModel();

        String fabio = "http://purl.org/spar/fabio/";
        model.setNsPrefix("fabio", fabio);
        Model fabioModel = ModelFactory.createDefaultModel();

        String dcat = "https://www.w3.org/TR/vocab-dcat-2/";
        model.setNsPrefix("dcat", dcat);
        Model dcatModel = ModelFactory.createDefaultModel();

        String prov = "https://www.w3.org/ns/prov/";
        model.setNsPrefix("prov", prov);
        Model provModel = ModelFactory.createDefaultModel();

        String prism = "http://prismstandard.org/namespaces/1.2/basic/";
        model.setNsPrefix("prism", prism);
        Model prismModel = ModelFactory.createDefaultModel();
        
        String schema = "http://schema.org/";
        model.setNsPrefix("schema", schema);
        Model schemaModel = ModelFactory.createDefaultModel();
        
        
        String bido = "http://purl.org/spar/bido/";
        model.setNsPrefix("bido", bido);
        Model bidoModel = ModelFactory.createDefaultModel();
        

     
        
        for (int i = 1; i < limite; i++) {
            
            atributos[i][0] = atributos[i][0].replace("\"", "");
            //System.out.println(atributos[i][1]);
            // AUTORES
            
            String id_autores = atributos[i][0];
            String[] parts_id_autores = id_autores.split(";");
            String nombres = atributos[i][1];
            String[] parts_nombres = nombres.split(";");
               
            // DOCUMENTOS}
            String eid_documento = atributos[i][2];
            String titulo = atributos[i][3];
           
        
            // URI DEL DOCUMENTO
            String URI_DOCUMENTO = dataPrefix + "BibliographicResource/" +  eid_documento;
            String source_title = atributos[i][4];
            String anio = atributos[i][5];
       
            String vol = atributos[i][6];
            String issue = atributos[i][7];
            String num_citas = atributos[i][12];
            
            String doi = atributos[i][17];
            String url = atributos[i][18];
            
            String document_type = atributos[i][13];
            //String stage = atributos[i][16];
            //String access = atributos[i][17];
      
            // Nombre de la fuente
            String fuente = atributos[i][14];
            String language = atributos[i][15];
            
            
            
            
            Resource lang = model.createResource(dbr+language)
                    .addProperty(RDF.type, schemaModel.getResource(schema + "Language"))
                    .addProperty(RDFS.label, language);
            
            // CREACION DE DOCUMENTO BIBLIOGRAFICO
            Resource documento = model.createResource(URI_DOCUMENTO)
                    .addProperty(DCTerms.title, titulo)
                    .addProperty(DCTerms.date, anio)
                    .addProperty(DCTerms.language, lang)
                    .addProperty(RDFS.subClassOf, fabioModel.getResource(fabio+ "ScholaryWork"))
                    .addProperty(prismModel.getProperty(prism +"doi"), doi)
                    //.addProperty(prismModel.getProperty(prism +"issn"), issn)
                    .addProperty(prismModel.getProperty(prism +"volume"), vol);
                    //.addProperty(provModel.getProperty(prov + "wasDerivedFrom"), datasetInfo)
                    //.addProperty(bidoModel.getProperty(bido+ "withBibliometricData"), myOntoModel.getResource(dataPrefix+"Source/"+issn));
            
             if(num_citas!= null){
                 documento.addProperty(myOntoModel.getProperty(dataPrefix +"citationsCount"), num_citas);
             }

            // Se crea el tipo de documento
            // Se compara el tipo con los de fabio y si es igual toma la uri de fabio
            tipo = false;
            for (String nombre : tipos_Fabio) {
                if(document_type.replace(" ","").equals(nombre)){
                    documento.addProperty(RDF.type, fabioModel.getResource(fabio + document_type.replace(" ","")));
                    tipo = true;
                    break;
                }
            }
            if(!tipo){
                documento.addProperty(RDF.type, myOntoModel.getResource(dataPrefix + document_type.replace(" ","")));
            }
            
       
            for (int j = 0; j < parts_id_autores.length; j++) {
                // CREANDO INSTANCIAS DE LOS AUTORES
                int long_parts_nombres = parts_nombres.length;
                String URI_AUTOR = dataPrefix+ "Author/" + parts_id_autores[j];
                Resource autor = model.createResource(URI_AUTOR)
                        .addProperty(RDF.type, myOntoModel.getResource(dataPrefix+"Author"))
                        .addProperty(RDFS.subClassOf, FOAF.Person);
                if (j<long_parts_nombres) {
                    autor.addProperty(FOAF.name, parts_nombres[j]);
                }
                // Vinculando el autor al documento
                documento.addProperty(DCTerms.creator, autor);

            }
            
        }


        StmtIterator iter = model.listStatements();
        // Print the triplets
        
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();  // get next statement
            Resource subject = stmt.getSubject();     // get the subject
            Property predicate = stmt.getPredicate();   // get the predicate
            RDFNode object = stmt.getObject();      // get the object

            System.out.print(subject.toString());
            System.out.print(" " + predicate.toString() + " ");
            if (object instanceof Resource) {
                System.out.print(object.toString());
            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }
            System.out.println(" .");
        }
        // Save to a file
        RDFWriter writer = model.getWriter("RDF/XML"); //RDF/XML
        writer.write(model, os, dataPrefix);
    }

    public static void leerDatos() {
        limite = 0;
        BufferedReader bufferLectura = null;
        try {
            // Abrir el .csv en buffer de lectura
            bufferLectura = new BufferedReader(new FileReader("src/main/resources/datosfuente.csv"));
            // Leer una linea del archivo
            String linea = bufferLectura.readLine();
            while (linea != null) {
                // Sepapar la linea leída con el separador definido previamente
                String[] campos = linea.split(SEPARADOR);

                atributos[limite] = linea.split(SEPARADOR);
                // Volver a leer otra línea del fichero
                linea = bufferLectura.readLine();
                limite = limite + 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Cierro el buffer de lectura
            if (bufferLectura != null) {
                try {
                    bufferLectura.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
