# Introduction #

The OpenMap Developer's Guide is an in-depth resource for how OpenMap components work.  This page is a summary of the high points, to give you a quick idea of what's what.


# Details #

<b><font size='+1'>Consider OMGraphics...</font></b>

<p>
OMGraphics are important, because they are how you represent data as<br>
objects on a map. OMGraphics come in different flavors -<br>
OMBitmap, OMCircle, OMLine, OMPoint, OMPoly, OMRaster, OMRect,<br>
OMText. An OMGraphicList can be used to contain OMGraphics, and<br>
is also an OMGraphic itself. This allows you to nest<br>
OMGraphicLists.<br>
<br>
<p>
When considering how to represent your data as OMGraphics, there are a<br>
couple of things to think about:<br>
<br>
<p>
You can create customized OMGraphics by combining standard OMGraphics<br>
into a new object that contains an OMGraphicList, or extend<br>
OMGraphicList to contain the OMGraphics you need, adding methods you<br>
require. The com.bbn.openmap.layer.location.Location object is<br>
an example of creating an object to contain OMGraphics, combining a<br>
generic OMGraphic marking a location, and a OMText object for the<br>
location label.<br>
<br>
<p>
OMGraphics contain an attribute map that can contain any other objects you need to  associate with the OMGraphic.  You can access these attributes using the putAttribute(key, value) and getAttribute(key, value) methods. This can be really<br>
handy to use to store more information about the data you are<br>
representing with that OMGraphic (web page addresses, additional<br>
attributes, etc).<br>
<br>
<p>
OMGraphics can be rendered in three ways, which are<br>
represented by renderType.<br>
<ul>
<li>
RENDERTYPE_LATLON means that the object should be placed<br>
on the map in its lat/lon location. You should expect the<br>
OMGraphic to scale the object as the map scale changes, and the<br>
object's location on the screen will change as the map location<br>
changes.</li>

<li>
RENDERTYPE_XY means the object should be placed at a<br>
screen pixel location on the map. The OMGraphic does not move or<br>
scale as the map projection changes.</li>

<li>
RENDERTYPE_OFFSET means the object should be placed at<br>
some screen pixel location offset from a lat/lon coordinate. The<br>
object will move with the map location changes, but will not scale if<br>
the map scale changes.</li>
</ul>

There are three different line types associated with OMGraphics that<br>
have lines rendered in lat/lon space. These setting do not<br>
affect OMGraphics with RENDERTYPE_XY or RENDERTYPE_OFFSET:<br>
<br>
<ul>
<li>
LINETYPE_STRAIGHT means the lines will be straight on<br>
the screen between points of the OMGraphic, regardless of the<br>
projection type.</li>

<li>
LINETYPE_GREATCIRCLE means that the lines drawn between<br>
points of the OMGraphic will be the shortest geographical distance<br>
between those points.</li>

<li>
LINETYPE_RHUMB means that the line drawn between points<br>
of the OMGraphics will be of constant bearing, or going in the same<br>
direction.</li>
</ul>

<b>Most importantly</b>, there is a paradigm you have to work<br>
in with OMGraphics. Once an OMGraphic is created, it <b>MUST</b> be<br>
projected, which means that its representation, in relation to the<br>
map, needs to be calculated. This is done by taking the<br>
Projection object that arrives in the ProjectionChanged event, and<br>
using it on the OMGraphic.generate(Projection) method. If the<br>
projection changes, the OMGraphics will need to be generated().<br>
If the position of the OMGraphic has changed, or certain attributes of<br>
the OMGraphic are changed, the OMGraphics needs to be generated.<br>
The OMGraphics are smart enough to know when a attribute change<br>
requires a generation, so go ahead an call it, and the OMGraphic will<br>
decide to do the work or not. If you try to render an OMGraphic<br>
that has not been generated, it will not appear on the map.<br>
After the OMGraphic is generated, the java.awt.Graphics object<br>
that arrives in the Layer.paint() method can be passed to the<br>
OMGraphic.render(java.awt.Graphics) method.  See the layer section<br>
below for more information about managing the Projection object for<br>
use with your OMGraphics.<br>
<br>
<p>
<b><font size='+1'>and Layers...</font></b>

<p>
Layers can do anything they want to in order to render<br>
their data on the map. When a layer is added to a map, it<br>
becomes a Java Swing component, so its rendering in relation to other<br>
layers on the map is taken care of automatically.<br>
<br>
<p>
When a layer is added to the MapBean, it automatically<br>
gets added as a ProjectionListener to the MapBean. That means<br>
that when the map changes, the layer will receive a ProjectionChanged<br>
event, letting it know what the new map projection looks like.<br>
It's then up to the layer to decide what it wants to draw on the<br>
screen based on that projection, and then call repaint() on itself<br>
when it is ready to have its paint() method called. The Java<br>
AWT event thread will then call paint() on the layer at the proper<br>
time. paint() can also be called automatically by the AWT<br>
thread, for map window repaints and when another layer asks to be<br>
repainted. paint() methods should not do more than simply render<br>
graphics that are currently on the map, in order to take up as little<br>
time as necessary with that AWT thread.<br>
<br>
<p>
The OMGraphicHandlerLayer is a super-class implementation of Layer that <b>does a lot of work for you</b>.  You should extend any layer you write from this class, and simply override the prepare() method to create and return OMGraphics to display on the map.  The prepare() method is called whenever the projection changes (pan, zoom, window resize).  The current projection can be retrieved in the prepare method by calling getProjection(), and this can be used to call generate(projection) on your OMGraphics before they are returned from this method.  You must do this.  <b>Look at the com.bbn.openmap.layer.learn package</b> to see best practices of creating layers to do different things.  The OMGraphicHandlerLayers in that package are well commented, and each demonstrate a certain aspect of managing and interacting with OMGraphics.<br>
<p>
The OMGraphicHandlerLayer automatically launches a separate thread when it calls prepare(), and automatically calls repaint() on itself when prepare() returns.  If you want to force a new thread to be created to call prepare() on the layer, call doPrepare().<br>
<br>
<p>
You can also use the OpenMap layers as examples of different ways to<br>
create and manage OMGraphics. The GraticuleLayer creates its<br>
OMGraphics internally, while the ShapeLayer reads data from a<br>
file. The DTED and RpfLayers have image caches. The<br>
CSVLocationLayer uses a quadtree to store OMGraphics. You can<br>
also access a spatial database to create OMGraphics. Any<br>
technique of managing graphics can be used within a layer.<br>
<br>
<p>
The LayerHandler object is used in the OpenMap<br>
application to manage layers - both those visible on the map, and<br>
those available for the map. The LayerHandler uses the<br>
Layer.isVisible() attribute to decide which layers are active on the<br>
map. It has methods to change the visibility of layers, add<br>
layers, remove layers, and change their order. It does not have<br>
a user interface, so it can be used with any application.<br>
<br>
<p>
For the OpenMap application, layers are added or removed<br>
by modifying the openmap.properties file. The property file<br>
contains instructions on how to do this. For OpenMap layers,<br>
their unique properties that can be set to initialize them should be<br>
listed in the layer's JavaDocs.<br>
<br>
<p>
The Layer.getGUI() method provides a way for a layer to<br>
create its user interface which can control its attributes. The<br>
getGUI() method should just return a java.awt.Component, which means<br>
you can customize it any way you want. The parent Layer class<br>
returns null by default if you decide not to provide a<br>
GUI.<br>
<br>
<p>
<b><font size='+1'>and Mouse Events....</font></b>

<p>
MouseEvents can be managed by certain OpenMap<br>
components, directing them to layers and to OMGraphics.<br>
MouseModes describe how MouseEvents and MouseMotionEvents are<br>
interpreted and consumed.<br>
<br>
<p>
The MouseDelegator is the real MouseListener and<br>
MouseMotionListener on the MapBean. The MouseDelegator manages a<br>
list of MouseModes, and knows which one is 'active' at any given<br>
time. The MouseDelegator also asks the active Layers for their<br>
MapMouseListeners, and adds the ones that are interested in events<br>
from the active MouseMode as listeners to that mode.<br>
<br>
<p>
When a MouseEvent gets fired from the MapBean, it goes<br>
through the MouseDelegator to the active MouseMode, where the<br>
MouseMode starts providing the MouseEvent to its<br>
MapMouseListeners. Each listener is given the chance to consume<br>
the event. A MapMouseListener is free to act on an event and not<br>
consume it, so that it can continue to be passed on to other<br>
listeners.<br>
<br>
<p>
From the Layer point of view, it has a method where it<br>
can be asked for its MapMouseListener. The Layer can implement<br>
the MapMouseListener interface, or it can delegate that responsibility<br>
to another object, or can just return null if it's not interested in<br>
receiving events (the Layer default). The MapMouseListener<br>
provides a String array of all the MouseMode ID strings it is<br>
interested in receiving events from, and also has its own methods<br>
that the MouseEvents and MouseMotionEvents arrive in. The<br>
MapMouseListener can use these events, combined with the<br>
OMGraphicList, to find out if events have occurred over any OMGraphics,<br>
and respond if necessary. Remember, if something on the layer<br>
changes as a result of an event, the layer can call repaint() on<br>
itself.<br>
<br>
<p>
Once again, the OMGraphicHandlerLayer <b>makes things easier for you" by handling MouseEvents over its OMGraphics.  Look at the</b>com.bbn.openmap.layer.learn.InteractionLayer<b>for instructions on how to take advantage of this functionality.</b>

<p>
PlugIns can also provide and/or implement the<br>
MapMouseListener interface - the PlugInLayer passes the request<br>
through to the PlugIn.<br>
<br>
<p>
<b><font size='+1'>and the BeanContext, a.k.a. the MapHandler...</font></b>

<p>
Understanding the MapHandler is one of the most important aspects of<br>
customizing an OpenMap application if you want to make the whole<br>
process pretty trivial.<br>
<br>
<p>
The MapHandler is a Java BeanContext, which is a big bucket where you<br>
can add or remove objects. If an object is a<br>
BeanContextMembershipListener, it will receive events when other<br>
objects get added to or removed from the BeanContext.<br>
<br>
<p>
The reason that the MapHandler (as opposed to simply using the<br>
BeanContext) exists is that it is an extended BeanContext that keeps<br>
track of SoloMapComponents. SoloMapComponent is an interface,<br>
and can be used to say that there is only supposed to be one instance<br>
of a component type in the BeanContext at a time. For instance,<br>
the MapBean is a SoloMapComponent, and there can only be one MapBean<br>
in a MapHandler at a time. The SoloMapComponentPolicy is an<br>
object that tells the MapHandler what to do if another MapBean (or<br>
other duplicate SMC instance) is added to the MapHandler, either<br>
rejecting the second instance of the MapBean, or replacing the<br>
previous MapBean.<br>
<br>
<p>
So, a MapHandler can be thought of as a Map, complete with the<br>
MapBean, Layers, and other management components that are contained<br>
within.<br>
<br>
<p>
That said, the MapHandler is incredibly useful. It can be used<br>
by objects that need to get a hold of other objects and<br>
services. It can be used to add or remove components to the<br>
application, at runtime, and all the other objects added to the<br>
MapHandler get notified of the addition/removal automatically.<br>
<br>
<p>
In the OpenMap application, the openmap.properties file has an<br>
openmap.components property that lists all the components that make up<br>
the application. To change the components in the application,<br>
edit this list.<br>
<br>
<p>
If you want your component to be told of the BeanContext, make it a<br>
BeanContextChild. It will get added to the MapHandler so that<br>
other components can find it, if it is on the openmap.components<br>
property list.  If you are creating your own components<br>
programmatically, simply add the BeanContextChild component to the<br>
MapHandler yourself.<br>
<br>
<p>
The com.bbn.openmap.MapHandlerChild is an abstract class that contains<br>
all the methods and fields necessary for an object to be a<br>
BeanContextChild and a BeanContextMembershipListener. If your<br>
object extends this class, you just have to implement the methods<br>
findAndInit() which is called whenever an object is added to the<br>
MapHandler, and childrenRemoved() which is called when objects are<br>
removed. You can use the Iterator that gets send to these<br>
methods to find other components that have been added to or removed<br>
from the application, and adjust your component accordingly.<br>
Make sure your component is stable if it doesn't find what it needs -<br>
you shouldn't assume that the other objects will be added in any<br>
particular order, or even added at all. Also, you should check<br>
that when objects are removed that the instance of the object is the<br>
same that is being used by your component before you disconnect from<br>
it (not just the same class type). As a MapHandlerChild, your<br>
component can be added to the OpenMap application without recompiling<br>
any OpenMap source code. You'll notice that the application<br>
class (com.bbn.openmap.app.OpenMap) is pretty basic, using the<br>
PropertyHandler to instantiate all the components and add them to the<br>
MapHandler.<br>
<br>
<p>
<b><font size='+1'>and the PropertyConsumer interface...</font></b>

<p>
The PropertyConsumer interface can be implemented by any component<br>
that wants to be able to configure itself with a java.awt.Properties<br>
object.  It also has methods that let it provide information about the<br>
properties it can use, and what they mean.<br>
<br>
<p>
In general, Properties are a set of key-value pairs, each defined as<br>
Java Strings.  The com.bbn.openmap.layer.util.LayerUtils class has<br>
methods that can be used to translate the value Java Strings into Java<br>
primitives and objects, like ints, floats, booleans, Color, etc.<br>
<br>
<p>
Several PropertyConsumers may have their properties defined in a<br>
single properties file, which is what happens when the OpenMap<br>
application uses the openmap.properties file.  In order for each<br>
PropertyConsumer to be able to figure out which properties are<br>
intended for it, the PropertyConsumer can be given a unique scoping<br>
property prefix string.  In the openmap.properties instructions, this<br>
scoping string is referred to as a marker name.  If the property prefix<br>
is set in a PropertyConsumer, it should prepend that string to each<br>
property key, separating them with a period.  For example a layer may<br>
have a property key called lineWidth, which tells it how thick to draw<br>
its line graphics.  If it is given a property prefix of layer1, it<br>
should check its properties for a 'layer1.lineWidth' property.  If the<br>
layer is given a null prefix (default), then it should look for a<br>
'lineWidth' property.<br>
<br>
<p>
The methods for the PropertyConsumer are:<br>
<br>
<ul>
<li>
setPropertyPrefix(String prefix) - set the scoping prefix.</li>
<li>
setProperties(Properties props) - provide the properties, with a null prefix.</li>
<li>
setProperties(String prefix, Properties props) - provide the<br>
properties with a prefix.</li>
<li>
getProperties(Properties props) - set the current values of the<br>
properties in the Properties object provided.  If Properties is null,<br>
create one to fill.  The keys in this Properties object should be<br>
scoped with a prefix if one is set.</li>
<li>
getPropertyInfo(Properties props) - set the metadata for the<br>
properties in the Properties object.  Again, if Properties is null,<br>
create one and fill it.  The keys in this Properties object should<br>
<i>NOT</i> be scoped, and the values for the keys should be a short<br>
explaination for what the property means.  The PropertyConsumer may<br>
also provide a 'key.editor' property here with the value a fully<br>
qualified class name of the<br>
com.bbn.openmap.util.propertyEditor.PropertyEditor to use to modify<br>
the value in a GUI, if needed.</li>
</ul>

<p>
PropertyConsumers can use the<br>
com.bbn.openmap.util.propertyEditor.Inspector to provide an interface<br>
to the user to configure it at runtime.  It also allows the<br>
PropertyConsumer to provide its current state for properties files<br>
being saved for later use.<br>
<br>
<p>
Lastly, when the OpenMap application is creating objects from the<br>
openmap.components property, the marker name on that list becomes the<br>
property prefix for components.  The ComponentFactory, which creates<br>
the components on behalf of the PropertyHandler, checks to see if the<br>
component is a PropertyConsumer, and if so it calls<br>
setProperties(prefix, properties) on it to let the component configure itself.<br>
<br>
<p>
<b><font size='+1'>and hints to help with certain application Design Patterns...</font></b>

<p>
The OpenMap application is really a framework.  The application can<br>
be adjusted and components swapped in and out by modifying the<br>
openmap.components property to create the components you want.  If<br>
you want to create your own application, it's likely that you can<br>
still use the OpenMap application for it and still completely<br>
customize it.<br>
<br>
<p>
You don't have to use Properties - feel free to create any object you want, programmatically, and simply add it to the MapHandler.<br>
<br>
<p>
If you want your layer to be driven by an external object, check out<br>
the com.bbn.openmap.plugin.graphicLoader package.  A GraphicLoader is<br>
an object that is able to provide OMGraphics to an OMGraphicHandler<br>
(which can be thought of as a receiver).  The graphicLoader package<br>
contains the AbstractGraphicLoader, an abstract GraphicLoader<br>
implementation that has a Swing Timer in it to trigger itself to<br>
deliver OMGraphic updates.  You can extend this class to customize how<br>
and when these updates occur. If you place the GraphicLoaderConnector<br>
in the MapHandler along with your GraphicLoaders, the<br>
GraphicLoaderConnector will create a GraphicLoaderPlugIn/PlugInLayer<br>
combination to listen to each GraphicLoader if the GraphicLoader<br>
doesn't already have a receiver specified.