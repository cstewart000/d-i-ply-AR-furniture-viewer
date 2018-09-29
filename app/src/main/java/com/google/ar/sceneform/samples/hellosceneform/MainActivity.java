package com.google.ar.sceneform.samples.hellosceneform;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Sun;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PixelCopy.OnPixelCopyFinishedListener {

    private static final String TAG = ArSceneformActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.1;

    private ArFragment arFragment;
    private ModelRenderable model1;
    private ModelRenderable model2;
    private ModelRenderable model3;
    private ModelRenderable model4;

    private TextView txtTitle;
    private String selectedModel = "";

    private DrawerLayout drawerLayout;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        drawerLayout = findViewById(R.id.drawer_layout);



        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);


        // TODO: Inflate menu programmatically from ENUM
        //Menu m = navigationView.getMenu();
        //MenuItem foo_menu_item=m.add("foo");


        ImageView expandMenuIcon=findViewById(R.id.expandMenuIcon);
        expandMenuIcon.setOnClickListener(view1 -> openOrCloseDrawer());

        ImageView photoIcon=findViewById(R.id.photoIcon);
        photoIcon.setOnClickListener(view1 -> takePhoto());



        txtTitle=findViewById(R.id.txtTitle);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().

        loadRenderableModels();


        if (arFragment != null) {

            arFragment.setOnTapArPlaneListener(
                    (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                        plotModel(hitResult);
                    });
        }

        drawerLayout.openDrawer((int) Gravity.LEFT);
    }

    private void loadRenderableModels() {

        ModelRenderable.builder()
                .setSource(this, R.raw.lean_ah_cen_hq)
                .build()
                .thenAccept(renderable -> model1 = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, R.string.unable_load_model, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.on_again_off_again_)
                .build()
                .thenAccept(renderable -> model2 = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, R.string.unable_load_model, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.the_rory)
                .build()
                .thenAccept(renderable -> model3 = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, R.string.unable_load_model, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });


        ModelRenderable.builder()
                .setSource(this, R.raw.glens_monitor_riser_2_tier)
                .build()
                .thenAccept(renderable -> model4 = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, R.string.unable_load_model, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }


    public void openOrCloseDrawer() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    private void plotModel(HitResult hitResult) {


        clearARscene();

        // Create the Anchor.
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());


        // Create the transformable activeModel and add it to the anchor.
        TransformableNode activeModel = new TransformableNode(arFragment.getTransformationSystem());

        //need to set scale BEFORE setting the parent! need to change the minimum scale
        activeModel.getScaleController().setMinScale(0.1f);
        activeModel.setLocalScale(new Vector3(0.3f, 0.3f, 0.3f));
        activeModel.setParent(anchorNode);


        switch (selectedModel) {
            case Models.Model_1: {
                if (model1 == null)
                    return;
                activeModel.setRenderable(model1);
            }
            break;
            case Models.Model_2: {
                if (model2 == null) return;
                activeModel.setRenderable(model2);
            }
            break;
            case Models.Model_3: {
                if (model3 == null) return;
                activeModel.setRenderable(model3);
            }
            break;
            case Models.Model_4: {
                if (model4 == null) return;
                activeModel.setRenderable(model4);
            }
            break;
        }
        //activeModel.setWorldScale(new Vector3(0.4f, 0.4f, 0.4f));
        //activeModel.setLocalScale(new Vector3(0.02f, 0.02f, 0.02f));
        activeModel.select();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()) {
            case R.id.menu_model_1:
                selectedModel = Models.Model_1;
                txtTitle.setText(ModelsEnum.LEAN_AH.getName());
                break;

                case R.id.menu_model_2:
                selectedModel = Models.Model_2;
                txtTitle.setText(Models.Model_2);
                break;
            case R.id.menu_model_3:
                selectedModel = Models.Model_3;
                txtTitle.setText(Models.Model_3);
                break;
            case R.id.menu_model_4:
                selectedModel = Models.Model_4;
                txtTitle.setText(Models.Model_4);
                break;
            case R.id.menu_about:

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("About this application");
                alertDialog.setMessage("Check furniture before you buy!\n\nw: d-i-ply.com" +
                        "\ne: cameron@d-i-ply.com\ndeveloped using Android's ARCore API\n\nDeveloped by: " +
                        "Cameron Stewart and Priyank Gandhi");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "SWEET AS BRO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.show();

                break;
            case R.id.menu_visit_website:
                intentToWebsite();
                break;

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    private void intentToWebsite() {

        String url = getResources().getString(R.string.website_url);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     * <p>
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.1 capabilities.
     * <p>
     * <p>Finishes the activity if Sceneform can not run
     */
    @SuppressLint("ObsoleteSdkInt")
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.1 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.1 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    private void clearARscene() {
        List<Node> children = new ArrayList<>(arFragment.getArSceneView().getScene().getChildren());
        for (Node node : children) {
            if (node instanceof AnchorNode) {
                if (((AnchorNode) node).getAnchor() != null) {
                    ((AnchorNode) node).getAnchor().detach();
                }
            }
            if (!(node instanceof Camera) && !(node instanceof Sun)) {
                node.setParent(null);
            }
        }
    }

    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + date + "_screenshot.jpg";
    }

    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }



    private void takePhoto() {
        final String filename = generateFilename();
        ArSceneView view = arFragment.getArSceneView();



        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.


        PixelCopy.request(view, bitmap, (int copyResult) -> {
            if (true) {

                try {
                    saveBitmapToDisk(bitmap, filename);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(MainActivity.this, e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Photo saved", Snackbar.LENGTH_LONG);
                snackbar.setAction("Open in Photos", v -> {
                    File photoFile = new File(filename);

                    Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                            MainActivity.this.getPackageName() + ".ar.codelab.name.provider",
                            photoFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
                    intent.setDataAndType(photoURI, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);

                });
                snackbar.show();
            } else {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    @Override
    public void onPixelCopyFinished(int i) {

    }


    @interface Models {

        //TODO: CS: Make ENUM hold data
        String Model_1 = "Lean-ah Shoe Rack";
        String Model_2 = "On-again-off-again Shelves";
        String Model_3 = "the Rory Record Cabinet";
        String Model_4 = "Glen's Screen Risers";
    }
}
