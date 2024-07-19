package io.github.gargee18.gargeedev.registration;

//specific libraries
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import io.github.rocsg.fijiyama.registration.ItkTransform;


public class RegisterXRandMRI {
    
    // public static String pathToTrInit = "/home/phukon/Desktop/MRI/02_ceps/2022_CEPS_suivi_clinique/transMatrices_2022_XR_MRI/";
    public static String mainDir = "/home/phukon/Desktop/";
    public static String trMatPath = "/home/phukon/Desktop/highresTransformationMatricesAndRegisteredImages/";
    public static String outPath = mainDir + "registeredLowResOfNewCroppedUnsignedDataXR/";
    public static String year1 = GeneralUtils.years[1];
    public static String year2 = GeneralUtils.years[2];


    public static void main(String[] args) {
 
        ImageJ ij = new ImageJ();
        
        String[] specimen = GeneralUtils.specimenListXR; 
        // get the tr matrix
        for (int i = 0; i < specimen.length; i ++){
            String imgRefpath = trMatPath+specimen[i]+"_TransformedImage_2022_2023.tif"; 
            //String imgRefpath = mainDir + "cropdata"+year1+"/CEP_"+specimen[i]+"_"+year1+"_XR_crop.tif";
            //String imgRefpath = mainDir + "XR_"+year1+"_raw/CEP_"+specimen[i]+"_"+year1+"_XR.tif";
            System.out.println(imgRefpath);
            String imgMovpath =  mainDir+"XR_"+year2+"_raw/CEP_"+specimen[i]+"_"+year2+"_XR.tif"; 
            System.out.println(imgMovpath);
            ImagePlus imgRef =IJ.openImage(imgRefpath);
            System.out.println(imgRef.getDisplayRangeMin());
            ImagePlus imgMov = IJ.openImage(imgMovpath);
            System.out.println(trMatPath+specimen[i]+"_transform_global_img_moving_"+year2+".txt");
            ItkTransform trMat=ItkTransform.readTransformFromFile(trMatPath+specimen[i]+"_transform_global_img_moving_"+year2+".txt");
            ImagePlus finalResult=trMat.transformImage(imgRef,imgMov);
            System.out.println(trMatPath+ specimen[i]+"_TransformedImage_"+year1+"_"+year2+".tif");
            IJ.saveAsTiff(finalResult,trMatPath+ specimen[i]+"_TransformedImage_"+year1+"_"+year2+".tif");
        

     
        }
    }

}



            //ItkTransform trFinal = RegistrationAutomate.autoLinearRegistration(imgRef,imgMov,trInit,specimen);
            // ImagePlus finalResult=trFinal.transformImage(imgRef,imgMov);
            // IJ.saveAsTiff(finalResult,mainDir+"_XR_MRI/result_automate/"+specimen+"_TransformedImage.tif");
            // ImagePlus imgComposite=VitimageUtils.compositeNoAdjustOf(imgRef, finalResult);
            // IJ.saveAsTiff(imgComposite,mainDir+"_XR_MRI/result_automate/"+specimen+"_TransformedImage.tif");
            // imgComposite.show();
            // imgComposite.close();
            // imgRef.close();
            // imgMov.close();