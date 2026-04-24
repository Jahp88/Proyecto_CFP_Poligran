import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Clase encargada de la generación de archivos de texto plano con información
 * pseudoaleatoria para pruebas del sistema de ventas.
 * Cumple con la creación de archivos de productos, vendedores y ventas.
 */
public class GenerateInfoFiles {

    // Arrays con datos de prueba estáticos para generar información aleatoria
    private static final String[] NOMBRES = {"Carlos", "Ana", "Luis", "Maria", "Juan", "Pedro", "Lucia", "Andres"};
    private static final String[] APELLIDOS = {"Gomez", "Perez", "Rodriguez", "Martinez", "Lopez", "Diaz", "Hernandez"};
    private static final String[] TIPOS_DOC = {"CC", "CE", "TI"};
    private static final String[] PRODUCTOS_NOMBRES = {"Laptop", "Mouse", "Teclado", "Monitor", "Impresora", "Audifonos"};
    private static final double[] PRODUCTOS_PRECIOS = {2500000.50, 80000.00, 150000.99, 950000.00, 450000.00, 120000.00};

    /**
     * Método principal que orquesta la generación de los archivos de prueba.
     * * @param args Argumentos de la línea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando generación de archivos de prueba...");
            createProductsFile(PRODUCTOS_NOMBRES.length);
            createSalesManInfoFile(5); // Generaremos 5 vendedores para la prueba
            System.out.println("¡Archivos generados exitosamente en la raíz del proyecto!");
        } catch (Exception e) {
            System.err.println("ERROR durante la generación: " + e.getMessage());
        }
    }

    /**
     * Crea un archivo plano con la información de los productos disponibles.
     * * @param productsCount Cantidad de productos a registrar en el archivo.
     * @throws Exception Si ocurre un error de Entrada/Salida al escribir el archivo.
     */
    public static void createProductsFile(int productsCount) throws Exception {
        try (PrintWriter writer = new PrintWriter("productos.csv", StandardCharsets.UTF_8.name())) {
            for (int i = 0; i < productsCount; i++) {
                // Formato: ID Producto; Nombre Producto; Precio
                writer.println((i + 1) + ";" + PRODUCTOS_NOMBRES[i] + ";" + PRODUCTOS_PRECIOS[i]);
            }
        }
    }

    /**
     * Crea un archivo plano con la información básica de múltiples vendedores y, 
     * a su vez, invoca la creación del archivo de ventas individual para cada uno.
     * * @param salesmanCount Cantidad de vendedores a generar.
     * @throws Exception Si ocurre un error al escribir el archivo.
     */
    public static void createSalesManInfoFile(int salesmanCount) throws Exception {
        Random rand = new Random();
        try (PrintWriter writer = new PrintWriter("vendedores.csv", StandardCharsets.UTF_8.name())) {
            for (int i = 0; i < salesmanCount; i++) {
                long id = 100000000 + rand.nextInt(900000000); // Genera ID de 9 dígitos
                String tipoDoc = TIPOS_DOC[rand.nextInt(TIPOS_DOC.length)];
                String nombre = NOMBRES[rand.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[rand.nextInt(APELLIDOS.length)];
                
                // Formato: Tipo Documento; Número Documento; Nombres; Apellidos
                writer.println(tipoDoc + ";" + id + ";" + nombre + ";" + apellido);
                
                // Generar entre 2 y 6 ventas aleatorias para este vendedor
                createSalesMenFile(rand.nextInt(5) + 2, nombre, id, tipoDoc);
            }
        }
    }

    /**
     * Crea el archivo plano individual de ventas para un vendedor específico.
     * * @param randomSalesCount Cantidad de registros de venta a generar.
     * @param name Nombre del vendedor (disponible por si se requiere expandir lógica).
     * @param id Número de documento del vendedor.
     * @param tipoDoc Tipo de documento del vendedor.
     * @throws Exception Si ocurre un error de Entrada/Salida.
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id, String tipoDoc) throws Exception {
        Random rand = new Random();
        String fileName = "vendedor_" + id + ".csv";
        
        try (PrintWriter writer = new PrintWriter(fileName, StandardCharsets.UTF_8.name())) {
            // Primera línea: Tipo Documento Vendedor; Número Documento Vendedor
            writer.println(tipoDoc + ";" + id);
            
            // Líneas de ventas: ID Producto; Cantidad Vendida;
            for (int i = 0; i < randomSalesCount; i++) {
                int idProducto = rand.nextInt(PRODUCTOS_NOMBRES.length) + 1;
                int cantidadVendida = rand.nextInt(15) + 1; 
                writer.println(idProducto + ";" + cantidadVendida + ";");
            }
            
            // Punto Extra: Inyectar datos anómalos aleatorios para probar validación (Cantidades negativas o ID inexistente)
            if (rand.nextBoolean()) {
                writer.println("999;-5;"); // ID falso y cantidad negativa para validar robustez
            }
        }
    }
}
