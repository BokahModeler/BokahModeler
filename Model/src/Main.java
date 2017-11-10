import org.lwjgl.opengl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.system.*;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;
public class Main {
	private boolean keyUp = false;
	private boolean keyDown = false;
	private boolean mousePressed = false;
    private long window;
    private GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private final int WIDTH = gd.getDisplayMode().getWidth() - 200;
    private final int HEIGHT = gd.getDisplayMode().getHeight() - 200;
    ArrayList<ArrayList<float[]>> nose = null;
    private static JFrame f;
    private static Loader load;
    
    /**
     * Initializes model and pulls in nose coordinates
     */
        public Main(){
    	Model model = new Model();
    	nose = model.getNose();
    }
    
    /**
     * Main execute method
     */
    public void execute() {
        try {
        	//initializes window
            init();
    		f.setVisible(false);
    		
    		//Keeps window open
            loop();
            
            //Destroys window
            glfwDestroyWindow(window);
        } finally {
        	System.exit(0);
            glfwTerminate();
        }
    }
  
    /**
     * Initializes window
     */
    private void init() {
    	GLFWErrorCallback.createPrint(System.err).set();
  
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");
  
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
  
        //Creates window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Game", NULL, NULL);
        
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");
  
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});
  
        try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }
 
    /**
     * Draws window
     * @param x
     * @param y
     */
    private void draw(double x, double y){
    	BufferedImage img = null;
    	
    	//Loads image
    	img = load.getImgFront();
//    	try{
//    		img = ImageIO.read(new File("models/front and side2.png"));
//    	}
//    	catch(IOException e){
//    		System.out.println("nofile");
//    	}
    	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    	glTranslated(0,0,0);//center of model(need to figure out what this is)
    	
    	glRotatef(1,(float)y,(float)x,0f);
    	 
    	glBindTexture(GL_TEXTURE_2D, getTexture(img));//must go before glbegin. will move inside loop once we have an array of textures.
        glBegin(GL_QUADS);
        
        //Draws polygons
        for(ArrayList<float[]> polygon: nose){//nose should be calculated in another class//will probably neeed a for loop to get texture
        	float[] topLeft = polygon.get(0);
        	float[] topRight = polygon.get(1);
        	float[] bottomRight = polygon.get(2);
        	float[] bottomLeft = polygon.get(3);
        	glTexCoord2f(0,0);						//for the textures
        	glVertex3f(topLeft[0], topLeft[1], topLeft[2]);
        	glTexCoord2f(1,0);
        	glVertex3f(topRight[0], topRight[1], topRight[2]);
        	glTexCoord2f(1,1);
        	glVertex3f(bottomRight[0], bottomRight[1], bottomRight[2]);
        	glTexCoord2f(0,1);
        	glVertex3f(bottomLeft[0], bottomLeft[1], bottomLeft[2]);
        }
        glEnd();
    	
    }
    
    /**
     * Draws texture
     * @param image
     * @return textureID
     */
   public int getTexture(BufferedImage image){//have a texture class
	   
	   //Pixel array
	   int[] pixels = new int[image.getWidth() * image.getHeight()*4];
       pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

       ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4); //4 for RGBA, 3 for RGB

       //Puts pixels into buffer
       for(int y = 0; y < image.getHeight(); y++){
           for(int x = 0; x < image.getWidth(); x++){
               int pixel = pixels[y * image.getWidth() + x];
               buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
               buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
               buffer.put((byte) (pixel & 0xFF));               // Blue component
               buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
           }
       }

       buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

       // You now have a ByteBuffer filled with the color data of each pixel.
       // Now just create a texture ID and bind it. Then you can load it using 
       // whatever OpenGL method you want, for example:

     int textureID = glGenTextures(); //Generate texture ID
       glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID

       //Setup wrap mode
       glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
       glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

       //Setup texture scaling filtering
       glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
       glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

       //Send texel data to OpenGL
       glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

       //Return the texture ID so we can bind it later again
     return textureID;
    }
   
   /**
    * Keeps window up
    */
    private void loop() {
    	
    	GL.createCapabilities();
    	glEnable(GL_TEXTURE_2D);
    	double newX, newY, prevx = 0, prevy = 0;
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		//Clears buffers
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        glLoadIdentity();
        draw(0.0,0.0);	//draw first image with no rotation
        
        glfwSwapBuffers(window);
        
        //Checks if mouse is pressed
        while ( !glfwWindowShouldClose(window)  ) {
            if(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS && !mousePressed){
        		//glfwSetCursorPos(window,HEIGHT,WIDTH/4);
        		prevx = HEIGHT/2;
        		prevy = WIDTH/2;
        		mousePressed = true;
        	}
            else if(!(glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1)== GLFW_PRESS)){
            	mousePressed = false;
            }
            
            //If mouse is pressed, and moves, rotate object
        	if(mousePressed){
        		DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer y = BufferUtils.createDoubleBuffer(1);
        		GLFW.glfwGetCursorPos(window, x, y);
        		newX = x.get(0);
        		newY = y.get(0);
        		double deltaX = newX-prevx;
        		double deltaY = newY-prevy;
        		draw(deltaX,deltaY);
        		glfwSwapBuffers(window);//updates window
        		prevx = newX;
        		prevy = newY;
        	}
        	
        	//Zoom in
        	if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
        		draw(HEIGHT / 2, WIDTH / 2);
        		glfwSwapBuffers(window);
        		glScaled(1.2, 1.2, 1);
        	}
        	
        	//Zoom out
        	if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
        		draw(HEIGHT / 2, WIDTH / 2);
        		glfwSwapBuffers(window);
        		glScaled(0.8, 0.8, 0.8);
        	}
        	
        	glfwPollEvents();
        }    		
    }
    
    /**
     * Main method
     * @param args
     */
    public static void main(String[] args) {

		//JFrame is the "frame" of the window
		 f = new JFrame("Load Image Sample");

		 //Adds a new window listener
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		//Adds loader to JFrame
		load = new Loader(f);
		f.add(load);
		f.pack();
		f.setVisible(true);
    }
}
