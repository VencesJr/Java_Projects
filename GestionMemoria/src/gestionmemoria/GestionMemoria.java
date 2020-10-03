package gestionmemoria;

import java.util.LinkedList;
import java.util.List;

 

public class GestionMemoria {

       // //////////////////////////////////////////////////////////////

       // IMPLEMENTACIÓN DE UNIDAD DE GESTIÓN DE MEMORIA EN JAVA ///////

       // Por Juan Carlos Roldán Salvador //////////////////////////////

       // Desarrollado en JavaSE-1.7 ///////////////////////////////////

       // Modificado por última vez el 3/12/2012 ///////////////////////

       // //////////////////////////////////////////////////////////////

       //

       // Esta es una implementación funcional de los métodos de primer,

       // siguiente, mejor y peor ajuste.

       // En ella se supone una memoria de 32 kB y una gestión mediante

       // listas de control con ticks de 1 kB. Cada uno de los bloques de

       // la lista de control es una array de int de 4 valores, que son:

       // 0: Estado (Un valor de 0 significa libre, un valor de 1 ocupado)

       // 1: Dirección inicial del bloque

       // 2: Tamaño (En ticks)

       // 3: Pid del proceso al que está asignado

       //

       // Para cambiar el tipo de ajuste que se aplica solo hay que

       // comentar/descomentar líneas de código de la función creaProceso.

       // Se puede cambiar el tamaño del mapa de memoria de 32 a cualquier

       // otro.

       // Para ver el estado de la memoria por pantalla se usa la función

       // imprimeMemoria(). Para crear y destruir procesos se usan las

       // funciones creaProceso y destruyeProceso, respectivamente.

       // En la función main (Al final de la clase) hay un ejemplo, con la

       // creación y destrucción de varios procesos.

       public static int LONGITUD_MEMORIA;

       public static List<int[]> listaControl;

       public static int[] memoria;

       public static int ultimoPid;

 

       /**

        * Inicia los atributos necesarios para el funcionamiento de la memoria.

        *

        */

       static void inicializaMemoria() {
           
             LONGITUD_MEMORIA = Integer.parseInt(Interfaz.txtTam.getText());

             listaControl = new LinkedList<int[]>();

             memoria = new int[LONGITUD_MEMORIA];

             ultimoPid = 0;

             // Añade una primera entrada a la lista de control, un hueco con el

             // tamaño de la memoria completa:

             int[] hueco = { 0, 0, LONGITUD_MEMORIA, 0 };

             listaControl.add(hueco);

       }

 

       /**

        * Simula la escritura de datos en la memoria. Guarda los datos en una

        * array, a modo de mapa de bits.

        *

        * @param direccion

        *            - En ticks, posición en que empieza la escritura.

        * @param tamanyo

        *            - Ticks que se extiende la escritura.

        * @param dato

        *            - Marca que se pondrá en la memoria, que en un caso real sería

        *            parte del proceso. Por ejemplo, '1' para memoria ocupada y '0'

        *            para memoria vacía.

        */

       private static void escribeMemoria(int direccion, int tamanyo, int dato) {

             //Escribe el dato 'dato' en las posiciones pertinentes:

             for (int i = 0; i < tamanyo; i++) {

                    memoria[direccion + i] = dato;

                    // Aquí se puede incluir un delay para simular el acceso a memoria:

                    /*

                     * try { Thread.sleep(3); } catch (InterruptedException e) {

                     * e.printStackTrace(); }

                     */

             }

       }

 

       /**

        * Asigna un bloque de memoria a un nuevo proceso.

        *

        * @param pid

        *            - Identificador numérico del proceso que se va a crear. El que

        *            la invoque tiene la responsabiidad de evitar que se repita.

        * @param tamanyo

        *            - Tamaño que necesita el proceso.

        * @return Devuelve <b>true</b> si se ha podido satisfacer la petición, o

        *         <b>false</b> en caso contrario.

        */

       public static boolean creaProceso(int pid, int tamanyo) {
           int hueco = 0;
           
           if (Interfaz.rbPrimer.isSelected())
                hueco = Ajustes.primerAjuste(listaControl, tamanyo);
           
           if (Interfaz.rbSiguiente.isSelected())
                hueco = Ajustes.siguienteAjuste(listaControl, tamanyo);

           if (Interfaz.rbMejor.isSelected())
                hueco = Ajustes.mejorAjuste(listaControl, tamanyo);

           if (Interfaz.rbPeor.isSelected())
                hueco = Ajustes.peorAjuste(listaControl, tamanyo);

             boolean res = (hueco != -1);

             if (res) {

                    int direcc = listaControl.get(hueco)[1];

                    int[] proceso = { 1, direcc, tamanyo, pid };

                    int espacioRestante = listaControl.get(hueco)[2] - tamanyo;

                    // Inserta el proceso en el lugar del hueco

                    listaControl.set(hueco, proceso);

                    // Si el proceso es más pequeño que el hueco, inserta un hueco

                    if (espacioRestante > 0) {

                           int[] bloqRestante = { 0, direcc + tamanyo, espacioRestante, 0 };

                           listaControl.add(hueco + 1, bloqRestante);

                    }

                    escribeMemoria(direcc, tamanyo, 1);

             }

             return res;

       }

 

       /**

        * Borra un proceso de la memoria.

        *

        * @param pid

        *            - Identificador numérico del proceso que se pretende terminar.

        * @return Devuelve <b>true</b> si se ha podido satisfacer la petición, o

        *         <b>false</b> en caso contrario.

        */

       public static boolean destruyeProceso(int pid) {

             // Busca el índice del proceso en la lista de control

             int indice = 0;

             for (int[] bloque : listaControl) {

                    indice++;

                    if (bloque[3] == pid) {

                           break;

                    }

             }

             indice--;

             boolean encontrado = (indice != -1);

             // Si lo ha encontrado, lo borra de la memoria y de la lista de control

             if (encontrado) {

                    int[] bloqueABorrar = listaControl.get(indice);

                    listaControl.get(indice)[0] = 0;

                    listaControl.get(indice)[3] = 0;

                    escribeMemoria(bloqueABorrar[1], bloqueABorrar[2], 0);

                    fusiona(indice);

                    fusiona(indice - 1);

             }

             return encontrado;

       }

 

       /**

        * Fusiona un bloque de memoria de la lista de control con su vecino

        * posterior, si ambos son bloques libres.

        *

        * @param indice

        *            - Posición en la lista de control del primer bloque de la

        *            fusión, siendo el segundo indice + 1.

        * @return Devuelve <b>true</b> en el caso de que ambos bloques tengan

        *         índices válidos, y estén vacíos, <b>false</b> en caso contrario.

        */

       private static boolean fusiona(int indice) {

             boolean fusionable = false;

             if (indice >= 0 && (indice + 1) < listaControl.size()) {

                    fusionable = listaControl.get(indice)[0] == 0

                                  && listaControl.get(indice + 1)[0] == 0;

             }

             if (fusionable) {

                    int tamanyo = listaControl.remove(indice + 1)[2];

                    listaControl.get(indice)[2] += tamanyo;

             }

             return fusionable;

       }

 

       /**

        * Muestra en pantalla el estado de la lista de control y la memoria.

        */

       public static String imprimeLista() {

             String s = "Lista de control: {[EST-DIR-TAM-PROC]:";

             for (int[] bloque : listaControl) {

                    s += "[" + bloque[0] + "-" + bloque[1] + "-" + bloque[2] + "-"

                                  + bloque[3];

                    s += "]";

             }

             s += "}";
             
             //System.out.println(s + "\n");
             
             return s;
       }
       
       public static String imprimeMemoria() {
           String m = "";
           
           for (int i : memoria) {

                    m += i;

             }
             
             m += "\n";
             
             return m;
       }

 

       // Este es el punto de entrada de la implementación.

// Se puede experimentar con las primitivas creadas.

       public static void main(String[] args) {                                  
            
           Interfaz v = new Interfaz();
           v.setVisible(true);                                                               
            
             //inicializaMemoria();
            
             /*
             creaProceso(1, 6);                                                   
             imprimeMemoria();
            

             creaProceso(2, 6);                
             imprimeMemoria();

            

             creaProceso(3, 8);             
             imprimeMemoria();

            

             creaProceso(4, 7);             
             imprimeMemoria();

            

            // destruyeProceso(2);

            // destruyeProceso(4);

             creaProceso(5, 7);              
             imprimeMemoria();

            

            // destruyeProceso(1);

             creaProceso(6,5);             
             imprimeMemoria();

            

            // destruyeProceso(5);

            // imprimeMemoria();

            

            // destruyeProceso(3);

            // destruyeProceso(6);

            // imprimeMemoria();
            
            */
       }

 

}