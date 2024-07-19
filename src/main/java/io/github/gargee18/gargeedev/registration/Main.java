package io.github.gargee18.gargeedev.registration;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

public class Main {
    public static String specimen = GeneralUtils.getXRSpecimenNameWithIndex(0);
    public static String[] specimenList = GeneralUtils.getSpecimenListXR();
    public static String year1 = "2022";
    public static String year2 = "2023";
     public static void main(String[] args) {
        ImageJ ij = new ImageJ();

        for (int i = 0; i < specimenList.length; i++) {
            String imgPathR = "/home/phukon/Desktop/Results_2024/Cep_Monitoring/XR/4_Registered_Images/8bit_Registered_2022_2023_2024/CEP_"
                    + specimenList[i] + "_" + year1 + "_XR.tif";// Red channel
            String imgPathG = "/home/phukon/Desktop/Results_2024/Cep_Monitoring/XR/4_Registered_Images/8bit_Registered_2022_2023_2024/CEP_"
                    + specimenList[i] + "_" + year2 + "_XR.tif";// Green channel
            String outputDir = "/home/phukon/Desktop/jointHistoandROI/roi_" + specimen + ".zip";

            ImagePlus imgR = IJ.openImage(imgPathR);
            ImagePlus imgG = IJ.openImage(imgPathG);
            // ImagePlus imgComposite = VitimageUtils.compositeNoAdjustOf(imgR, imgG) ;
            ImagePlus[] jointHistograms = JointHistogramBasedSegmentation.computeAndDisplayJointHistogramsWithNoBinSize(imgR, imgG);
            jointHistograms[0].show();
            jointHistograms[0].setTitle(specimenList[i]+"_"+year1+"_"+year2+"_Probability.tif");
            jointHistograms[1].show();
            jointHistograms[1].setTitle(specimenList[i]+"_"+year1+"_"+year2+"_LogProbability.tif");
            jointHistograms[2].show();
            jointHistograms[2].setTitle(specimenList[i]+"_"+year1+"_"+year2+"_MutualProbability.tif");

            IJ.save(jointHistograms[0],"/home/phukon/Desktop/Results_2024/Cep_Monitoring/XR/5_Histogram/jointHistogram/" + specimenList[i] + "_" + year1 + "_" + year2 + "_Probability.tif");
            IJ.save(jointHistograms[1],"/home/phukon/Desktop/jointHistoandROI/"+specimenList[i]+"_"+year1+"_"+year2+"_LogProbability.tif");
            System.out.println("Saved to"+"/home/phukon/Desktop/jointHistoandROI/"+specimenList[i]+"_"+year1+"_"+year2+"_LogProbability.tif");
            IJ.save(jointHistograms[2],"/home/phukon/Desktop/jointHistoandROI/"+specimenList[i]+"_"+year1+"_"+year2+"_MutualProbability.tif");
            System.out.println("Saved to"+"/home/phukon/Desktop/jointHistoandROI/"+specimenList[i]+"_"+year1+"_"+year2+"_MutualProbability.tif");

            ImagePlus segmentation=JointHistogramBasedSegmentation.makeSegmentationBasedOnRoiInputFromTheUser(imgR,imgG,jointHistograms[0],jointHistograms[1],specimen,outputDir);
            segmentation.setTitle("Segmentation");
            IJ.run(segmentation,"Fire","");
            segmentation.show();
            IJ.save(segmentation,"/home/phukon/Desktop/jointHistoandROI/"+specimen+"_"+year1+"_"+year2+"_segmented");
        }
        System.out.println("fini");
    }
}

