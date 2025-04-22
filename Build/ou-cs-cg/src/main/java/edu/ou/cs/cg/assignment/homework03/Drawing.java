
package edu.ou.cs.cg.assignment.homework03;

//import java.lang.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.nio.channels.Pipe;
import java.text.DecimalFormat;
import java.util.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.*;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import edu.ou.cs.cg.utilities.Utilities;
import java.awt.Rectangle;

public final class Drawing
{
    private static final GLU glu = new GLU();
    private Drawing() {}

	public static void drawBase(GL2 gl){
		gl.glPushMatrix();
		//gl.glRotatef(90.0f,1.0f,0.0f,0.0f);

		// bottom 
		gl.glPushMatrix();
		gl.glScalef(1.0f, 0.2f, 1.0f);
		drawCylinder(gl);
		gl.glPopMatrix();
		
		// first lip
		gl.glPushMatrix();
		gl.glScalef(0.9f, 0.9f, 0.9f);
		gl.glTranslatef(0f, 0.4f, 0f);
		drawLip(gl);
		gl.glPopMatrix();

		// horn
		gl.glPushMatrix();
		gl.glTranslatef(0f, .5f, 0f);
		gl.glScalef(0.8f, 0.3f, 0.8f);
		drawHorn(gl, 0.6f);
		gl.glPopMatrix();
		
		// second lip
		gl.glPushMatrix();
		gl.glTranslatef(0f, 1.1f, 0f);
		gl.glScalef(0.55f, 0.5f, 0.55f);
		drawLip(gl);
		gl.glPopMatrix();

		// second horn
		gl.glPushMatrix();
		gl.glTranslatef(0f, 1.0f, 0f);
		gl.glScalef(0.5f, 1.0f, 0.5f);
		drawHorn(gl, 0.8f);
		gl.glPopMatrix();
		
		// third lip
		gl.glPushMatrix();
		gl.glTranslatef(0f, 2.9f, 0f);
		gl.glScalef(0.45f, 0.4f, 0.45f);
		drawLip(gl);
		gl.glPopMatrix();
		
		// forth lip
		gl.glPushMatrix();
		gl.glTranslatef(0f, 3.0f, 0f);
		gl.glScalef(0.4f, 0.4f, 0.4f);
		drawLip(gl);
		gl.glPopMatrix();
		
		// fifth lip
		gl.glPushMatrix();
		gl.glTranslatef(0f, 3.1f, 0f);
		gl.glScalef(0.45f, 0.4f, 0.45f);
		drawLip(gl);
		gl.glPopMatrix();

		gl.glPopMatrix();
	}
	
	public static void drawBishop(GL2 gl){
		gl.glPushMatrix();
		drawBase(gl);
		gl.glTranslatef(0f, 3.1f, 0f);
		gl.glScalef(0.9f, 0.9f, 0.9f);
		drawBishopHead(gl);
		gl.glPopMatrix();
	}

	public static void drawKing(GL2 gl){
		gl.glPushMatrix();
		drawBase(gl);
		gl.glTranslatef(0f, 3.2f, 0f);
		gl.glScalef(1.3f, 1.3f, 1.3f);
		drawKingHead(gl);
		gl.glPopMatrix();
	}

	public static void drawQueen(GL2 gl){
		gl.glPushMatrix();
		drawBase(gl);
		gl.glTranslatef(0f, 3.7f, 0f);
		gl.glScalef(1.0f, 1.0f, 1.0f);
		drawQueenHead(gl);
		gl.glScalef(0.7f, 0.01f, 0.7f);
		drawCylinder(gl);
		gl.glPopMatrix();
	}
	
	public static void drawKnight(GL2 gl){
		gl.glPushMatrix();
		drawBase(gl);
		gl.glTranslatef(0f, 3.7f, 0f);
		
		gl.glScalef(2.0f, 2.0f, 2.0f);
		drawKnightHead(gl);
		gl.glPopMatrix();
	}
	public static void drawKnightHead(GL2 gl) {
		
	}
	
	
	public static void drawQueenHead(GL2 gl) {
		gl.glPushMatrix();
	
		// crown 
		gl.glScaled(1.0f,-1.0f,1.0f);
		gl.glScaled(0.7f,0.3f,0.7f);
		drawHorn(gl, 0.5f);

		// gl.glPushMatrix();
		// gl.glScaled(0.7f,0.7f,0.7f);
		// gl.glTranslatef(0f, -0.8f, 0f);
		// drawHorn(gl, 0.5f);
		// gl.glPopMatrix();

		// horns 
		// crenellations, small blocks around the top
		gl.glPushMatrix();
		gl.glTranslated(0, 1.2, 0);
		int crenelCount = 9;
		for (int i = 0; i < crenelCount; i++) {
			double angle = i * 360.0 / crenelCount;
			gl.glPushMatrix();
			gl.glRotated(angle, 0, 1, 0);
			gl.glTranslated(0.8, -1.4, 0);
			gl.glScaled(0.2, 0.4, 0.2);
			gl.glRotatef(90.0f, 0.0f, 0.0f, 1.0f);
			drawCylinder(gl);
			gl.glPopMatrix();
		}
		gl.glPopMatrix();
	
		gl.glPopMatrix();
	}
	
	public static void drawRook(GL2 gl, GLUT glut){
		gl.glPushMatrix();
		drawBase(gl);
		gl.glTranslatef(0f, 3.1f, 0f);
		gl.glScalef(0.6f, 0.4f, 0.6f);

		// top part.
		drawCylinder(gl);
		
		// crenellations, small blocks around the top
		gl.glPushMatrix();
		gl.glTranslated(0, 1.2, 0);
		int crenelCount = 6;
		for (int i = 0; i < crenelCount; i++) {
			double angle = i * 360.0 / crenelCount;
			gl.glPushMatrix();
			gl.glRotated(angle, 0, 1, 0);
			gl.glTranslated(0.9, 0, 0);
			gl.glScaled(0.2, 0.4, 0.2);
			glut.glutSolidCube(1);
			gl.glPopMatrix();
		}
		gl.glPopMatrix();

		gl.glPopMatrix();
	}
	public static void drawKingHead(GL2 gl) {
		GLU glu = new GLU();
		GLUT glut = new GLUT();
	
		gl.glPushMatrix();
	
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, 0.3f, 0.0f);
		gl.glScalef(0.4f, 0.4f, 0.4f);
		glut.glutSolidSphere(1.0, 20, 20);
		gl.glPopMatrix();
	
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, -0.1f, 0.0f);
		gl.glRotatef(-90, 1, 0, 0);
		glu.gluCylinder(glu.gluNewQuadric(), 0.2, 0.2, 0.3, 16, 1);
		gl.glPopMatrix();
	
		// cross
		gl.glScalef(2.0f, 2.2f, 2.0f);
		gl.glTranslatef(0.0f, -0.3f, 0.0f);
		gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);

		gl.glPushMatrix();
		gl.glTranslatef(0.0f, 0.7f, 0.0f);
		gl.glScalef(0.05f, 0.25f, 0.05f);
		glut.glutSolidCube(1.0f);
		gl.glPopMatrix();
	
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, 0.7f, 0.0f);
		gl.glScalef(0.25f, 0.05f, 0.05f);
		glut.glutSolidCube(1.0f);
		gl.glPopMatrix();
	
		gl.glPopMatrix();
	}
	
	public static void drawBishopHead(GL2 gl){
		gl.glPushMatrix();
        GLUquadric quad = glu.gluNewQuadric();
		float baseWidth = 0.6f;
		float height = 1.0f;
		int stacks = 40;
		float[] radii = new float[stacks];
		float[] heights = new float[stacks];

		float step = height / (stacks - 1);

		float n = 2.0f;  
		float m = 1.5f;  

		for (int i = 0; i < stacks; i++) {
			float t = (float)i / (stacks - 1);  

			float taper = (float)Math.pow(1 - Math.pow(t, n), m);
			float inward = 0.8f + 0.2f * (float)Math.sin(t * Math.PI); 
			radii[i] = baseWidth * taper * inward;
		
			heights[i] = t * height;
		}
				
		gl.glEnable(GL2.GL_STENCIL_TEST);
		gl.glClear(GL2.GL_STENCIL_BUFFER_BIT);

		gl.glColorMask(false, false, false, false); 
		gl.glDepthMask(false);                      
		gl.glStencilFunc(GL2.GL_ALWAYS, 1, 0xFF);   
		gl.glStencilOp(GL2.GL_REPLACE, GL2.GL_REPLACE, GL2.GL_REPLACE);

		gl.glPushMatrix();
		gl.glRotatef(45f, 0f, 1f, 0f); 
		gl.glRotatef(45f, 0f, 0f, 1f);  
		gl.glTranslatef(0f, 0.6f, 0f);  
		gl.glScalef(0.02f, 1.0f, 0.3f); 
		drawCutterShape(gl); 
		gl.glPopMatrix();

		gl.glColorMask(true, true, true, true);
		gl.glDepthMask(true);
		gl.glStencilFunc(GL2.GL_NOTEQUAL, 1, 0xFF); 
		gl.glStencilOp(GL2.GL_KEEP, GL2.GL_KEEP, GL2.GL_KEEP);

		drawProfile(gl, heights, radii, stacks);

		gl.glDisable(GL2.GL_STENCIL_TEST);
		
		gl.glTranslated(0, 1.0f, 0);
		glu.gluSphere(quad, 0.2, 24, 24);
		gl.glPopMatrix();
        glu.gluDeleteQuadric(quad);
	}

	public static void drawCutterShape(GL2 gl) {
		gl.glPushMatrix();
	
		// Move and rotate to intersect the bishop head
		gl.glTranslatef(0.0f, 0.6f, 0.0f);  // position higher up
		gl.glRotatef(45f, 0f, 1f, 0f);      // diagonal angle around Y axis
		gl.glScalef(0.05f, 1.0f, 0.3f);     // thin in X, tall in Y, deep in Z
	
		// Draw cube centered at origin (scaled above to form a cutting prism)
		gl.glBegin(GL2.GL_QUADS);
	
		// Front
		gl.glVertex3f(-0.5f, -0.5f,  0.5f);
		gl.glVertex3f( 0.5f, -0.5f,  0.5f);
		gl.glVertex3f( 0.5f,  0.5f,  0.5f);
		gl.glVertex3f(-0.5f,  0.5f,  0.5f);
	
		// Back
		gl.glVertex3f(-0.5f, -0.5f, -0.5f);
		gl.glVertex3f( 0.5f, -0.5f, -0.5f);
		gl.glVertex3f( 0.5f,  0.5f, -0.5f);
		gl.glVertex3f(-0.5f,  0.5f, -0.5f);
	
		// Left
		gl.glVertex3f(-0.5f, -0.5f, -0.5f);
		gl.glVertex3f(-0.5f, -0.5f,  0.5f);
		gl.glVertex3f(-0.5f,  0.5f,  0.5f);
		gl.glVertex3f(-0.5f,  0.5f, -0.5f);
	
		// Right
		gl.glVertex3f(0.5f, -0.5f, -0.5f);
		gl.glVertex3f(0.5f, -0.5f,  0.5f);
		gl.glVertex3f(0.5f,  0.5f,  0.5f);
		gl.glVertex3f(0.5f,  0.5f, -0.5f);
	
		// Top
		gl.glVertex3f(-0.5f,  0.5f, -0.5f);
		gl.glVertex3f( 0.5f,  0.5f, -0.5f);
		gl.glVertex3f( 0.5f,  0.5f,  0.5f);
		gl.glVertex3f(-0.5f,  0.5f,  0.5f);
	
		// Bottom
		gl.glVertex3f(-0.5f, -0.5f, -0.5f);
		gl.glVertex3f( 0.5f, -0.5f, -0.5f);
		gl.glVertex3f( 0.5f, -0.5f,  0.5f);
		gl.glVertex3f(-0.5f, -0.5f,  0.5f);
	
		gl.glEnd();
	
		gl.glPopMatrix();
	}
	
	public static void drawHorn(GL2 gl, float topRatio){
		
		float baseWidth = 1.0f;
		float topWidth = topRatio * baseWidth;
		int stacks = 20;
		float[] radii = new float[stacks];
		float[] heights = new float[stacks];

		for (int i = 1; i < stacks; i++){
			heights[i] = i/10.0f;
		}

		for (int i = 0; i < stacks; i++) {
			float t = (float)i / (stacks - 1); 
			radii[i] = topWidth + (baseWidth - topWidth) * (1 - t) * (1 - t);
		}

		drawProfile(gl, heights, radii, stacks);
	}

	public static void drawLip(GL2 gl){
        GLUquadric quad = glu.gluNewQuadric();
		gl.glPushMatrix();

        float height = .2f;
        float radius = 0.9f;
        float width = radius;

		int slices = 16;
		int stacks = 8;

		float[] heights = new float[stacks];
		float[] radii = new float[stacks];
		for(int i = 0; i < stacks; i++){
		}

		float step = height/(float)stacks;
		float maxAngle = (float)(Math.PI/2);
		//glu.gluDisk(quad, 0, width, stacks, 1);

		for (int i = 0; i < stacks; i++) {
			float t = (float)i / (stacks - 1);       
			float angle = t * maxAngle;              
			radii[i] = width + (height * (float)Math.cos(angle)); 
			heights[i] = (height/(float)stacks) * i;
		}


		float[] doubledRadii = new float[radii.length * 2];
		float[] doubledHeights = new float[heights.length * 2];

		System.arraycopy(radii, 0, doubledRadii, 0, radii.length);
		System.arraycopy(heights, 0, doubledHeights, 0, heights.length);
		
		for (int i = 0; i < radii.length; i++) {
			doubledRadii[radii.length + i] = radii[i];
		}
		
		for (int i = 0; i < heights.length; i++) {
			doubledHeights[heights.length + i] = - step * (i + 1);
		}

		drawProfile(gl, doubledHeights, doubledRadii, slices);
		gl.glPopMatrix();
        glu.gluDeleteQuadric(quad);
	}
	
	public static void drawProfile(GL2 gl, float[] heights, float[] radii, int slices) {
		if (heights.length != radii.length || heights.length < 2) return;
	
		gl.glBegin(GL2.GL_QUAD_STRIP);
		for (int i = 0; i < heights.length - 1; i++) {
			float y0 = heights[i];       // Lower Y
			float y1 = heights[i + 1];   // Higher Y
			float r0 = radii[i];
			float r1 = radii[i + 1];
	
			for (int j = 0; j <= slices; j++) {
				float theta = (float)(2 * Math.PI * j / slices);
				float cosTheta = (float)Math.cos(theta);
				float sinTheta = (float)Math.sin(theta);
	
				float x0 = r0 * cosTheta;
				float z0 = r0 * sinTheta;
				float x1 = r1 * cosTheta;
				float z1 = r1 * sinTheta;
	
				gl.glNormal3f(cosTheta, 0f, sinTheta);  // crude normal
				gl.glVertex3f(x0, y0, z0);  // lower ring
				gl.glVertex3f(x1, y1, z1);  // upper ring
			}
		}
		gl.glEnd();
	}
	
    public static void drawCylinder(GL2 gl) {
        GLUquadric quad = glu.gluNewQuadric();
        gl.glPushMatrix();

        //gl.glTranslatef(0f, -1f, 0f);
        gl.glRotatef(-90f, 1f, 0f, 0f);

        float height = 1.0f;
        float radius = 1.0f;

        glu.gluCylinder(quad, radius, radius, height, 32, 8);
        glu.gluDisk(quad, 0.0, radius, 32, 1);
        gl.glTranslatef(0f, 0f, height);
        glu.gluDisk(quad, 0.0, radius, 32, 1);

        gl.glPopMatrix();
        glu.gluDeleteQuadric(quad);
    }

	public static class Point3D {
        public float x, y, z;

        public Point3D(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return String.format("(%.3f, %.3f, %.3f)", x, y, z);
        }
    }
}

//******************************************************************************
