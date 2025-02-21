
/* Actual comment :
 * 
 *  The purpose of this step is to perform daisy-chain BM registration from time i to time i+1 (1 reg on 0 , 2 on 1... )
 *  (but in the geometry of the inoculation point and save the corresponding matrices
 * 
 * 
 */


package io.github.gargee18.gargeedev.cuttings.processing;


//specific libraries
import ij.IJ;
import ij.ImageJ;
// import ij.ImageJ;
import ij.ImagePlus;
import inra.ijpb.morphology.Morphology;
import io.github.rocsg.fijiyama.common.ItkImagePlusInterface;
import io.github.rocsg.fijiyama.common.VitimageUtils;
import io.github.rocsg.fijiyama.fijiyamaplugin.RegistrationAction;
import io.github.gargee18.gargeedev.cuttings.core.Config;
import io.github.gargee18.gargeedev.cuttings.core.PipelineStep;
import io.github.gargee18.gargeedev.cuttings.core.Specimen;
import io.github.rocsg.fijiyama.registration.BlockMatchingRegistration;
import io.github.rocsg.fijiyama.registration.ItkTransform;
import io.github.rocsg.fijiyama.registration.Transform3DType;



public class Step_3_RegistrationRigid implements PipelineStep{
    public static void main(String[] args) throws Exception{
        ImageJ ij=new ImageJ();
        Specimen spec= new Specimen("B_202");
        new Step_3_RegistrationRigid().execute(spec,true); 
        // seeResultsOfRigidRegistration(spec, 0);
    }
 

    @Override
    public void execute(Specimen specimen) throws Exception {
        execute(specimen,false);
    }
    
    
    public void execute(Specimen specimen,boolean testing) throws Exception {
        String[] days=Config.timestamps;
        int N=days.length;
        // BlockMatchingRegistration.setRandomSelection();
//        ItkImagePlusInterface.setSpeedupOnForFiji();
        for(int i=0;i<N-1;i++){
            int indexRef=i;
            int indexMov=i+1;
            
            //Open images
            ImagePlus imgRef=IJ.openImage(Config.getPathToSubsampledImage(specimen,indexRef));
            ImagePlus imgMov=IJ.openImage(Config.getPathToSubsampledImage(specimen,indexMov));
            ImagePlus imgRefToInoc=null;
            ImagePlus imgMovToInoc=null;
            // Get path to mask
            String mask = Config.getPathToMask(specimen, indexRef);
            
            ItkTransform trInocRef= ItkTransform.readTransformFromFile(Config.getPathToInoculationAlignmentTransformation(specimen,indexRef));
            ItkTransform trInocMov= ItkTransform.readTransformFromFile(Config.getPathToInoculationAlignmentTransformation(specimen,indexMov));
            System.out.println(trInocRef);
            System.out.println(trInocMov);
            //Apply the inoc transform to the reference image, to build a reference image aligned to inoc.
            //And do the same for the moving image
            imgRefToInoc=trInocRef.transformImage(imgRef,imgRef);
            imgMovToInoc=trInocMov.transformImage(imgRef,imgMov);

            imgRefToInoc.setTitle("Ref");
            imgMovToInoc.setTitle("Mov");   

            // Register to get the rigid matrix that align properly both images, but in the inoc space
            ItkTransform trRigid0 = autoLinearRegistrationWithPith(imgRefToInoc, imgMovToInoc, null, specimen, mask,testing);
            trRigid0.writeMatrixTransformToFile(Config.getPathToRigidRegistrationMatrix(specimen,indexRef, indexMov));    


        }
    }
    

    public static ItkTransform autoLinearRegistrationWithPith(ImagePlus imgRef, ImagePlus imgMov, ItkTransform trInit, Specimen specimen, String pathToMask,boolean testing){  
        RegistrationAction regAct = new RegistrationAction();
        regAct.defineSettingsFromTwoImages(imgRef, imgMov, null, false);
        regAct.typeTrans=Transform3DType.RIGID;
        // regAct.typeAutoDisplay=2;   
        regAct.typeAutoDisplay=(testing ? 2 : 0);   
        regAct.higherAcc=0; 
        regAct.levelMaxLinear=2;
        regAct.levelMinLinear=(testing ? 2 : 1);
        regAct.bhsX=3;
        regAct.bhsY=3;
        regAct.bhsZ=3;
        regAct.strideX=3;
        regAct.strideY=3;
        regAct.strideZ=3;
        regAct.iterationsBMLin=(testing ? 1 : 8);
        regAct.neighX=2;
        regAct.neighX=2;
        regAct.neighZ=2;

        BlockMatchingRegistration bmRegistration = BlockMatchingRegistration.setupBlockMatchingRegistration(imgRef, imgMov, regAct);
        bmRegistration.mask=IJ.openImage(pathToMask);
        bmRegistration.mask=Morphology.dilation(bmRegistration.mask, inra.ijpb.morphology.strel.DiskStrel.fromRadius(10));
        VitimageUtils.printImageResume(imgRef);
        VitimageUtils.printImageResume(imgMov);
        VitimageUtils.printImageResume(bmRegistration.mask);
       
        ItkTransform trFinal=bmRegistration.runBlockMatching(null, false);
        bmRegistration.closeLastImages();
        bmRegistration.freeMemory();
        return trFinal;
    }

    public static void seeResultsOfRigidRegistration(Specimen specimen, int step){
        int indexRef = step;
        int indexMov = step+1;

        // raw images
        ImagePlus imgRef = IJ.openImage(Config.getPathToNormalizedImage(specimen,indexRef));
        imgRef.show();
        ImagePlus imgMov = IJ.openImage(Config.getPathToNormalizedImage(specimen,indexMov));
        imgMov.show();

        // inoc aligned ref image
        ImagePlus imgRefToInoc=imgRef;
        ItkTransform trInocRef = ItkTransform.readTransformFromFile(Config.getPathToInoculationAlignmentTransformation(specimen,indexRef));
        imgRefToInoc=trInocRef.transformImage(imgRef,imgRefToInoc);
        imgRefToInoc.show();
        
        // inoc aligned mov image
        ImagePlus imgMovToInoc=imgMov;
        ItkTransform trInocMov = ItkTransform.readTransformFromFile(Config.getPathToInoculationAlignmentTransformation(specimen,indexMov));
        imgMovToInoc=trInocMov.transformImage(imgMov,imgMovToInoc);
        imgMovToInoc.show();

        // show composite of the images after inoc alignment
        ImagePlus imgAfterInocAlign = VitimageUtils.compositeNoAdjustOf(imgRefToInoc, imgMovToInoc, "composite");
        imgAfterInocAlign.show();
        imgAfterInocAlign.setTitle("Composite of images after alignment");
       
        ItkTransform tr=ItkTransform.readTransformFromFile(Config.getPathToRigidRegistrationMatrix(specimen, indexRef, indexMov));
        System.out.println(Config.getPathToRigidRegistrationMatrix(specimen, indexRef, indexMov));
        // ItkTransform trMov = trInocMov.addTransform(tr); 
        ImagePlus movRegistered=tr.transformImage(imgRefToInoc, imgMovToInoc);

        // show composite of the images after registration 
        ImagePlus imgAfterReg  = VitimageUtils.compositeNoAdjustOf(imgRefToInoc, movRegistered, "composite");
        imgAfterReg.show();
        imgAfterReg.setTitle("Composite of images after registration");

    }
    
}


         