package jpen.test;


import java.awt.BorderLayout;
import java.awt.Container;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JApplet;
import javax.swing.JFrame;

import jpen.bridge.BridgedPenEvent;
import jpen.bridge.BridgedPenListener;
import jpen.bridge.BridgedPenManager;
import jpen.bridge.PenButton;

import com.sun.opengl.util.BufferUtil;

public class TestCapture extends JApplet {
	
//	static {
//		try {
//		TEST_AvailableProviders.main(new String[0]);
//		
//		}
//		catch (Throwable t) {
//			t.printStackTrace();
//		}
//	}
	
	
	
	private static float[] sb = new float[2048];
	
	private static float[] SHARED_BUFFER(final int n) {
		if (sb.length < n) {
			final float[] _sb = new float[2 * n];
			System.arraycopy(sb, 0, _sb, 0, sb.length);
			sb = _sb;
		}
		return sb;
	}
	
	

	public static float invSqrt(float x) {
		float xhalf = 0.5f*x;
	    int i = Float.floatToIntBits(x);
	    i = 0x5f3759df - (i>>1);
	    x = Float.intBitsToFloat(i);
	    x = x*(1.5f - xhalf*x*x);
	    return x;
//		return  1.f / (float) Math.sqrt(x);
	}
	
	private static final class Frame {
		public final float x;
		public final float y;
		public final float pressure;
		
		public Frame(final float _x, final float _y, final float _pressure) {
			this.x = _x;
			this.y = _y;
			this.pressure = _pressure;
		}
		
		
		public float distance(final Frame other) {
			final float dx = x - other.x;
			final float dy = y - other.y;
			return (float) Math.sqrt(dx * dx + dy * dy);
		}
	}
	
	private static final class CompletedFrames {
		public final List<Frame> frames;
		public int vboId = -1;
		public int count = -1;
		
		public CompletedFrames(final Collection<Frame> _frames) {
			frames = new ArrayList<Frame>(_frames);
		}
	}
	
	public static void init(Container c) {
		
//		System.out.println(Utils.getDistVersion());
		
		
		final List<Frame> frames = new ArrayList<Frame>(1024);
		final List<CompletedFrames> allcframes = new ArrayList<CompletedFrames>(32);
		
		final Frame[] pframe = {null};
		
		final GL[] glref = {null};
		
		
		final GLEventListener glel = new GLEventListener() {
			private GLU glu;
			
			@Override
			public void init(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
		        glu = new GLU();
		        gl.glClearColor(1.f, 1.f, 1.f, 1.f);
		      
		        //gl.glClearDepth(0);
		        
		        // We manage our own depth, so disable this test (big perf boost):
		        //gl.glDisable(GL.GL_DEPTH_TEST);
//		        gl.glEnable(GL.GL_DEPTH_TEST);
//		        gl.glDepthFunc(GL.GL_ALWAYS);
		        // Disable v-sync if we can:
		        // TODO:
			    gl.setSwapInterval(0);
		        
			    //gl.glEnable(GL.GL_BLEND);
			    
			}

			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y,
					int width, int height) {
				
				GL gl = drawable.getGL();

				glref[0] = gl;
				gl.glViewport(x, y, width, height);
		        gl.glMatrixMode(GL.GL_PROJECTION);
		        gl.glLoadIdentity();
		        // Flip around the y-axis:
		        glu.gluOrtho2D(0.0, (double) width, (double) height, 0.0);
//		        gl.glOrtho(0.0, (double) width, (double) height, 0.0, 
//		        		0.0, -(0x01 << 15));

//		        glu.gluLookAt(width / 2.0, height / 2.0, 0, 
//		        		width / 2.0, height / 2.0, 2048, 
//		        		0.0, 1.0, 0.0);
		        gl.glMatrixMode(GL.GL_MODELVIEW);
		        gl.glLoadIdentity();
		        //gl.glTranslatef(width / 2.f, height / 2.f, 0.f);
//		        gl.glScalef(1.f, -1.f, 1.f);
//		        gl.glTranslatef(0.f, -height, 0.f);
				
			}
			
			@Override
			public void display(GLAutoDrawable drawable) {
				GL gl = drawable.getGL();
				
//				gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
				gl.glClear(GL.GL_COLOR_BUFFER_BIT);
				
				
				//gl.glColor3f(0.3f, 0.3f, 0.3f);
				gl.glColor3f(64/255.f, 64/255.f, 120/255.f);
//				gl.glLineWidth(2.f);
				
				/*
				long time0 = System.nanoTime();
				for (int i = 1; i < frames.size(); ++i) {
					final Frame frame0 = frames.get(i - 1);
					final Frame frame1 = frames.get(i);
					
					final float size = 0.25f + 12 * (frame0.pressure + frame1.pressure) / 2.f;
					gl.glLineWidth(size);
					gl.glBegin(GL.GL_LINES);
					gl.glVertex2f(frame0.x, frame0.y);
					gl.glVertex2f(frame1.x, frame1.y);
					gl.glEnd();
				}
				long time1 = System.nanoTime();
				
				System.out.println((time1 - time0) + "    " + frames.size() + "    " + (frames.isEmpty() ? 0 : (time1 - time0) / frames.size()));
				*/
				display2(gl);
			}
			
			private void display2(GL gl) {
				
				gl.glDepthMask(true);
				
				long time0 = System.nanoTime();
				
				renderCompletedFrames(gl);
				
				
				if (2 <= frames.size() && 5 <= frames.size()) {
					renderFrames(gl, frames);
//					renderFrames2(gl, frames);
				}
				
				long time1 = System.nanoTime();
				
				int count = 0;
				count += frames.size() * 2;
				for (CompletedFrames cframes : allcframes) {
					count += cframes.frames.size() * 2;
				}
				
//				System.out.println((time1 - time0) + "  ::  " + count);
			}
			
			private void renderCompletedFrames(final GL gl) {
				for (int i = 0; i < allcframes.size(); ++i) {
					final CompletedFrames cframes = allcframes.get(i);
					if (cframes.frames.size() < 2) continue;
					if (cframes.vboId < 0) {
						// attempt to create
						final int len = createCoords(cframes.frames);
						final float[] coords = sb;
						cframes.count = len / 2;
						
						FloatBuffer buffer = BufferUtil.newFloatBuffer(len);
						buffer.put(coords, 0, len);
						buffer.flip();
						
						final int[] vbo = {-1};
						gl.glGenBuffersARB(1, vbo, 0);
			            gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vbo[0]);
			            gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, 
			            		cframes.count * 2 * BufferUtil.SIZEOF_FLOAT, buffer, 
			            		GL.GL_STATIC_DRAW_ARB);

			            cframes.vboId = vbo[0];
					}
					
					if (cframes.vboId < 0) {
						renderFrames(gl, cframes.frames);
					}
					else {
						//gl.glMatrixMode(GL.GL_MODELVIEW);
						gl.glPushMatrix();
						//gl.glLoadIdentity();
						final int ID = i;
//						gl.glTranslatef(0.f, 0.f, (1+ ID));
						
						// render VBO:
						gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
						
						gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, cframes.vboId);
						gl.glVertexPointer(2, GL.GL_FLOAT, 0, 0);
						
						gl.glDrawArrays(GL.GL_QUAD_STRIP, 0, cframes.count);

			            gl.glDisableClientState(GL.GL_VERTEX_ARRAY);	
			            
			            gl.glPopMatrix();
					}
				}
			}
			
			private void renderFrames(final GL gl, final List<Frame> frames) {
				final int len = createCoords(frames);
				final float[] coords = sb;
				
//				gl.glBegin(GL.GL_LINE_STRIP);
//				for (int i = 0; i + 1 < coords.length; i += 2) {
//					gl.glVertex2f(coords[i], coords[i + 1]);
//				}
//				gl.glEnd();
				
				gl.glBegin(GL.GL_QUAD_STRIP);
				for (int i = 0; i + 1 < len; i += 2) {
					gl.glVertex2f(coords[i], coords[i + 1]);
				}
				gl.glEnd();
			}
			
			
			
			
			private void renderFrames2(final GL gl, final List<Frame> frames) {
				final int len = createCoords2(frames);
				final float[] coords = sb;
				
//				gl.glBegin(GL.GL_LINE_STRIP);
//				for (int i = 0; i + 1 < coords.length; i += 2) {
//					gl.glVertex2f(coords[i], coords[i + 1]);
//				}
//				gl.glEnd();
				
				float netPressure = 0.f;
				for (Frame frame : frames) {
					netPressure += frame.pressure;
				}
				netPressure /= frames.size();
				
				float th = 0.25f + 3 * netPressure;
				
				gl.glLineWidth(2.5f  * th);
				
				gl.glBegin(GL.GL_LINE_STRIP);
				for (int i = 0; i + 1 < len; i += 2) {
					gl.glVertex2f(coords[i], coords[i + 1]);
				}
				gl.glEnd();
			}
			

			@Override
			public void displayChanged(GLAutoDrawable drawable,
					boolean modeChanged, boolean deviceChanged) {
				
			}

			

			
			
		};
		
		
		final GLCapabilities caps = new GLCapabilities();
		caps.setDepthBits(16);
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);
		final GLCanvas canvas = new GLCanvas(caps);
		canvas.addGLEventListener(glel);
		
		
		final BridgedPenListener bpl = new BridgedPenListener() {

			private float daccum = 0.f;
			private void changed(final BridgedPenEvent e) {
				final float INITIAL_DISTANCE = 10.f;
				final float OTHER_DISTANCE = 2.5f;
				
				if (e.getButtons().contains(PenButton.TIP)) {
				final Frame frame = new Frame(e.getX(), e.getY(), 
						e.getPressure());
				if (null != pframe[0]) {
				daccum += frame.distance(pframe[0]);
				}
				if ((frames.size() <= 1 ? INITIAL_DISTANCE : OTHER_DISTANCE) <= daccum) {
					frames.add(frame);
					pframe[0] = frame;
					daccum = 0.f;
				}
				canvas.repaint();
				}
				
				
			}
			
			
			@Override
			public void penButtonPressed(BridgedPenEvent e) {
				if (e.getButtons().contains(PenButton.ERASER)) {
					if (null != glref[0]) {
						final GL gl = glref[0];
						
						final int x = (int) e.getX();
						final int y = (int) e.getY();
						final FloatBuffer db = BufferUtil.newFloatBuffer(8);
						db.rewind();
						//gl.glCopyPixels(x, y, 1, 1, GL.GL_DEPTH);
						//gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
						gl.glReadPixels(x, y, 1, 1,
								GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, db);
						for (int i = 0; i < db.capacity(); ++i) {
							float value = db.get(i);
							System.out.println("  ID: " + value);
						}
						
					}
				}
				
				if (! e.getButtons().contains(PenButton.TIP)) {
					if (5 <= frames.size()) {
						// push frame
						allcframes.add(new CompletedFrames(frames));
					}
					frames.clear();
				}
				else {
					final Frame frame = new Frame(e.getX(), 
							e.getY(), e.getPressure());
					frames.add(frame);
					pframe[0] = frame;
				}
			}
			
			@Override
			public void penMoved(BridgedPenEvent e) {
				changed(e);
			}
			
			@Override
			public void penPressed(BridgedPenEvent e) {
				changed(e);
			}
			
			@Override
			public void penScrolled(BridgedPenEvent e) {
				changed(e);
			}
			
			@Override
			public void penTilted(BridgedPenEvent e) {
				changed(e);
			}
			
			@Override
			public void penModeChanged(BridgedPenEvent e) {
			}
		};
		
		final BridgedPenManager bpm = new BridgedPenManager();
		bpm.addBridgedPenListener(canvas, bpl);
		
		
//		final PenManager pm = new PenManager(canvas);
		

//		final PenAccumulator accum = new PenAccumulator();
//		
//		final PenListener pl = new PenAdapter() {
//			@Override
//			public void penKindEvent(PKindEvent e) {
//			}
//			
//			@Override
//			public void penButtonEvent(PButtonEvent e) {
//				if (accum.pressedRight) {
//					if (null != glref[0]) {
//						final GL gl = glref[0];
//						
//						final int x = (int) accum.x;
//						final int y = (int) accum.y;
//						final FloatBuffer db = BufferUtil.newFloatBuffer(8);
//						db.rewind();
//						//gl.glCopyPixels(x, y, 1, 1, GL.GL_DEPTH);
//						//gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
//						gl.glReadPixels(x, y, 1, 1,
//								GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, db);
//						for (int i = 0; i < db.capacity(); ++i) {
//							float value = db.get(i);
//							System.out.println("  ID: " + value);
//						}
//						
//					}
//				}
//				
//				if (! accum.pressedLeft) {
//					if (5 <= frames.size()) {
//						// push frame
//						allcframes.add(new CompletedFrames(frames));
//					}
//					frames.clear();
//				}
//				else {
//					final Frame frame = new Frame(accum.x, accum.y, accum.pressure);
//					frames.add(frame);
//					pframe[0] = frame;
//				}
//			}
//			
//			private float daccum = 0.f;
//			@Override
//			public void penLevelEvent(PLevelEvent e) {
//				
//				
//				final float INITIAL_DISTANCE = 10.f;
//				final float OTHER_DISTANCE = 2.5f;
//				
////				float x = 0.f;
////				float y = 0.f;
////				float pressure = 0.f;
////				System.out.println("##");
////				for (PLevel level : e.levels) {
////					System.out.println("    " + level.getType() + " : " + level.value);
////				}
//				
//				//System.out.println(String.format("(%f, %f, %f)", accum.x, accum.y, accum.pressure));
//				if (accum.pressedLeft) {
//				final Frame frame = new Frame(accum.x, accum.y, accum.getPressure());
//				daccum += frame.distance(pframe[0]);
//				if ((frames.size() <= 1 ? INITIAL_DISTANCE : OTHER_DISTANCE) <= daccum) {
//					frames.add(frame);
//					pframe[0] = frame;
//					daccum = 0.f;
//				}
//				
////				System.out.println("*");
//				
//				//canvas.display();
//				canvas.repaint();
//				}
//			}
//		};
		
//		accum.delegate = pl;
//		
//		pm.pen.addListener(new SwingPenAdapter(accum));
		
		
		c.setLayout(new BorderLayout());
		
		c.add(canvas, BorderLayout.CENTER);
		

		
		
	}
	
	
	private static int createCoords2(final List<Frame> frames) {
		final int N = frames.size();
		
		final int len = 2 * N;
		final float[] coords = SHARED_BUFFER(len);
		
		int ci = 0;
		for (int i = 0; i < frames.size(); ++i) {
			final Frame frame = frames.get(i);
			coords[ci] = frame.x;
			coords[ci + 1] = frame.y;
			ci += 2;
		}
		
//		return coords;
		return len;
	}
	
	private static int createCoords(final List<Frame> frames) {
		final int N = frames.size();
		
		final int len = 2 * 2 * (N);
		final float[] coords = SHARED_BUFFER(len);
		int ci = 0;
		
		//final float th = 6.f;
		//final float hth = th / 2.f;
		
		// for each segment, compute the normal
		// for each side, average normals for neighbors -- that is the vector for the pinch point
		// multiply this vector by the thickness
		
		// store prev top normal, bottom normal
		
//		float[] ptop = new float[2];
//		float[] pbot = new float[2];
//		float[] top = new float[2];
//		float[] bot = new float[2];
		float ptopx;
		float ptopy;
		float pbotx;
		float pboty;
		float topx;
		float topy;
		float botx;
		float boty;
		
		Frame pframe = frames.get(0);
		Frame frame = frames.get(1);
		
		float dx = frame.x - pframe.x;
		float dy = frame.y - pframe.y;
		float n = invSqrt(dx * dx + dy * dy);
		dx *= n;
		dy *= n;
		
		ptopx = dy;
		ptopy = -dx;
		pbotx = -dy;
		pboty = dx;
		
		float th = 0.25f + 3 * pframe.pressure;
		float hth = th / 2.f;
		
		// TODO: write two vertices
		coords[ci] = pframe.x + ptopx * hth;
		coords[ci + 1] = pframe.y + ptopy * hth;
		coords[ci + 2] = pframe.x + pbotx * hth;
		coords[ci + 3] = pframe.y + pboty * hth;
		ci += 4;
		
		pframe = frame;
		
		for (int i = 2; i < N; ++i, pframe = frame, ptopx = topx, ptopy = topy, pbotx = botx, pboty = boty) {
			frame = frames.get(i);
			
			dx = frame.x - pframe.x;
			dy = frame.y - pframe.y;
			n = invSqrt(dx * dx + dy * dy);
			dx *= n;
			dy *= n;
			
			topx = dy;
			topy = -dx;
			botx = -dy;
			boty = dx;
			
			th = 0.25f + 3 * pframe.pressure;
			hth = th / 2.f;
			
			// TODO: average these with previous, write two more vertices
			
//			if (1 == (ci/4) % 2) {
//				// bottom first
//				coords[ci] = pframe.x + (pbotx + botx) * hth;
//				coords[ci + 1] = pframe.y + (pboty + boty) * hth;
//				coords[ci + 2] = pframe.x + (ptopx + topx) * hth;
//				coords[ci + 3] = pframe.y + (ptopy + topy) * hth;
//			}
//			else {
				// top first
				coords[ci] = pframe.x + (ptopx + topx) * hth;
				coords[ci + 1] = pframe.y + (ptopy + topy) * hth;
				coords[ci + 2] = pframe.x + (pbotx + botx) * hth;
				coords[ci + 3] = pframe.y + (pboty + boty) * hth;
//			}
			
			ci += 4;
			
		}
		
		th = 0.25f + 3 * pframe.pressure;
		hth = th / 2;
		// TODO: ptopXY and pbotXY contain the last. write the last two vertices
//		if (1 == (ci/4) % 2) {
//			// bottom first
//			coords[ci] = pframe.x + pbotx * th;
//			coords[ci + 1] = pframe.y + pboty * th;
//			coords[ci + 2] = pframe.x + ptopx * th;
//			coords[ci + 3] = pframe.y + ptopy * th;
//		}
//		else {
			// top first:
			coords[ci] = pframe.x + ptopx * hth;
			coords[ci + 1] = pframe.y + ptopy * hth;
			coords[ci + 2] = pframe.x + pbotx * hth;
			coords[ci + 3] = pframe.y + pboty * hth;
//		}
		ci += 4;
		
//		return coords;
		return len;
	}
	
	
	
//	public static void main(final String[] in) {
//		System.out.println(invSqrt(4));
//		
//	}
	
	
	
	public static void main(final String[] in) {
		final JFrame frame = new JFrame();
		final Container c = frame.getContentPane();
		
		init(c);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setVisible(true);
	}
	
	
	
	public TestCapture() {
	}
	
	
	
	@Override
	public void init() {
		init(this);
	}
	
	@Override
	public void start() {
	}
	
	@Override
	public void stop() {
	}
}
