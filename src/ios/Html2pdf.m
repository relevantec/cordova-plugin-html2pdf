/*
 Copyright 2014 Modern Alchemists OG

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

/*
 * Html 2 Pdf iOS Code: Clément Wehrung <cwehrung@nurves.com> (https://github.com/iclems/iOS-htmltopdf)
 */

#import "Html2pdf.h"

@interface Html2pdf (Private)

- (BOOL) saveHtml:(NSString*)html asPdf:(NSString*)filePath;

@end

@interface UIPrintPageRenderer (PDF)

- (NSData*) printToPDF;

@end

@implementation Html2pdf

static NSString *topLeft;
static NSString *topRight;
static NSString *bottomLeft;
static NSString *bottomRight;
static NSString *bottomLeftTemplate;
static NSString *bottomRightTemplate;
static int itemCount = 0;
static int topMargin;
static int bottomMargin;

@synthesize command, filePath, pageSize, pageMargins, documentController;

- (void)create:(CDVInvokedUrlCommand*)cmd
{
    self.command = cmd;

    @autoreleasepool {

        NSArray* arguments = cmd.arguments;

        // NSLog(@"Creating pdf from html has been started.");

        NSString* html = [arguments objectAtIndex:0];
        self.filePath  = [[arguments objectAtIndex:1] stringByExpandingTildeInPath];
        NSDictionary* options = [arguments objectAtIndex:2];

        topLeft = [options objectForKey:@"topLeft"];
        topRight = [options objectForKey:@"topRight"];
        bottomLeftTemplate = [options objectForKey:@"bottomLeft"];
        bottomRightTemplate = [options objectForKey:@"bottomRight"];

        topMargin = [[options objectForKey:@"topMargin"] floatValue];
        bottomMargin = [[options objectForKey:@"bottomMargin"] floatValue];
        if (![[options objectForKey:@"keepCounter"] boolValue]) {
            itemCount = 0;
        }

        // Set the base URL to be the www directory.
        NSString* wwwFilePath = [[NSBundle mainBundle] pathForResource:@"www" ofType:nil];
        NSURL*    baseURL     = [NSURL fileURLWithPath:wwwFilePath];

        // define page size and margins
        self.pageSize = kPaperSizeA4;
        self.pageMargins = UIEdgeInsetsMake(0, 5, 0, 5);

        // Load page into a webview and use its formatter to print the page
        UIWebView* webPage    = [[UIWebView alloc] init];
        webPage.delegate = self;
        webPage.frame = CGRectMake(0, 0, 1, 1); // Make web view small ...
        webPage.alpha = 0.0;                    // ... and invisible.
        [self.webView.superview addSubview:webPage];

        [webPage loadHTMLString:html baseURL:baseURL];
    }
}

- (void)mergePDFs:(CDVInvokedUrlCommand*)cmd
{
    command = cmd;

    @autoreleasepool {

        NSArray* arguments = cmd.arguments;
        filePath  = [[arguments objectAtIndex:0] stringByExpandingTildeInPath];
        NSArray* files = [arguments objectAtIndex:1];

        CFURLRef outputURL = (CFURLRef)CFBridgingRetain([[NSURL alloc] initFileURLWithPath:filePath]);
        CGContextRef writeContext = CGPDFContextCreateWithURL(outputURL, NULL, NULL);

        // Loop variables
        NSString* input;
        CFURLRef inputURL;
        CGPDFDocumentRef inputREF;
        NSInteger inputPages;
        CGPDFPageRef page;
        CGRect mediaBox;
        NSError *error = nil;
        NSFileManager *fileManager = [NSFileManager defaultManager];
        int i, j;

        //[[arguments objectAtIndex:1] stringByExpandingTildeInPath];

        for (i = 0; i < (int)[files count]; i++) {
            @autoreleasepool {
                input = [[files objectAtIndex:i] stringByExpandingTildeInPath];
                inputURL = (CFURLRef)CFBridgingRetain([[NSURL alloc] initFileURLWithPath:input]);
                inputREF = CGPDFDocumentCreateWithURL((CFURLRef) inputURL);
                inputPages = CGPDFDocumentGetNumberOfPages(inputREF);

                // Read the first PDF and generate the output pages
                for (j = 1; j <= inputPages; j++) {
                    @autoreleasepool {
                        // NSLog(@"prcesing %i %i", i, j);
                        page = CGPDFDocumentGetPage(inputREF, j);
                        mediaBox = CGPDFPageGetBoxRect(page, kCGPDFMediaBox);
                        CGContextBeginPage(writeContext, &mediaBox);
                        CGContextDrawPDFPage(writeContext, page);
                        CGContextEndPage(writeContext);
                        page = nil;
                    }
                }

                CFRelease(inputURL);
                CGPDFDocumentRelease(inputREF);
                [fileManager removeItemAtPath:input error:&error];
            }
        }

        // Finalize the output file
        CGPDFContextClose(writeContext);
        CGContextRelease(writeContext);
        CFRelease(outputURL);
        input = nil;
        fileManager = nil;
        error = nil;

        [self success];
    }
}

- (void)success
{
    @autoreleasepool {
        NSString* resultMsg = [NSString stringWithFormat:@"HTMLtoPDF did succeed (%@)", self.filePath];
        // NSLog(@"%@",resultMsg);

        // create acordova result
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                    messageAsString:[resultMsg stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];

        // send cordova result
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
}

- (void)error:(NSString*)message
{
    @autoreleasepool {
        NSString* resultMsg = [NSString stringWithFormat:@"HTMLtoPDF did fail (%@)", message];
        // NSLog(@"%@",resultMsg);

        // create cordova result
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                    messageAsString:[resultMsg stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];

        // send cordova result
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    @autoreleasepool {
        // NSLog(@"Html2Pdf webViewDidFinishLoad");

        UIPrintPageRenderer *render = [[UIPrintPageRenderer alloc] init];
        render.headerHeight = topMargin;
        render.footerHeight = bottomMargin;

        [render addPrintFormatter:webView.viewPrintFormatter startingAtPageAtIndex:0];

        CGRect printableRect = CGRectMake(self.pageMargins.left,
                                          self.pageMargins.top,
                                          self.pageSize.width - self.pageMargins.left - self.pageMargins.right,
                                          self.pageSize.height - self.pageMargins.top - self.pageMargins.bottom);

        CGRect paperRect = CGRectMake(0, 0, self.pageSize.width, self.pageSize.height);

        [render setValue:[NSValue valueWithCGRect:paperRect] forKey:@"paperRect"];
        [render setValue:[NSValue valueWithCGRect:printableRect] forKey:@"printableRect"];

        // NSLog(@"filePath %@", filePath);

        [[render printToPDF] writeToFile: filePath atomically: YES];

        // remove webPage
        [webView stopLoading];
        webView.delegate = nil;
        [webView removeFromSuperview];
        webView = nil;

        // trigger success response
        [self success];

        // Disable show "open pdf with ..." menu
        return;

        // show "open pdf with ..." menu
        NSURL* url = [NSURL fileURLWithPath:filePath];
        self.documentController = [UIDocumentInteractionController interactionControllerWithURL:url];

        documentController.delegate = self;

        UIView* view = self.webView.superview;
        CGRect rect = view.frame; // open in top center
        rect.size.height *= 0.02;

        BOOL isValid = [documentController presentOpenInMenuFromRect:rect inView:view animated:YES];

        if (!isValid) {
            NSString* messageString = [NSString stringWithFormat:@"No PDF reader was found on your device. Please download a PDF reader (eg. iBooks or Acrobat)."];

            UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Error" message:messageString delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [alertView show];
            //[alertView release]; // p. leak
        }
    }
}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    // NSLog(@"webViewDidFailLoadWithError");

    // trigger error response
    [self error:[error description]];
}

@end


@implementation UIPrintPageRenderer (PDF)


- (NSData*) printToPDF
{
    NSMutableData *pdfData = [NSMutableData data];

    UIGraphicsBeginPDFContextToData( pdfData, self.paperRect, nil );

    [self prepareForDrawingPages: NSMakeRange(0, self.numberOfPages)];

    CGRect bounds = UIGraphicsGetPDFContextBounds();

    NSString *pagesCount = [NSString stringWithFormat:@"%i", (int) self.numberOfPages];

    for ( int i = 0 ; i < self.numberOfPages ; i++ )
    {
        @autoreleasepool {
            UIGraphicsBeginPDFPage();
            itemCount++;

            NSString *pageNumber = [NSString stringWithFormat:@"%d", itemCount];
            bottomLeft = [bottomLeftTemplate stringByReplacingOccurrencesOfString:@"{{item_num}}"
                                                               withString:pageNumber
                          ];
            bottomRight = [bottomRightTemplate stringByReplacingOccurrencesOfString:@"{{item_num}}"
                                                               withString:pageNumber
                          ];
            bottomLeft = [bottomLeft stringByReplacingOccurrencesOfString:@"{{item_count}}"
                                                                       withString:pagesCount
                          ];
            bottomRight = [bottomRight stringByReplacingOccurrencesOfString:@"{{item_count}}"
                                                                        withString:pagesCount
                           ];

            [self drawPageAtIndex: i inRect: bounds];
            [self drawHeaderForPageAtIndex: i inRect: bounds];
            [self drawFooterForPageAtIndex: i inRect: bounds];
        }

    }

    UIGraphicsEndPDFContext();

    return pdfData;
}

- (void)drawHeaderForPageAtIndex:(NSInteger)pageIndex
                          inRect:(CGRect)headerRect {

    @autoreleasepool {
        NSMutableParagraphStyle *textStyle = [[NSMutableParagraphStyle defaultParagraphStyle] mutableCopy];
        textStyle.lineBreakMode = NSLineBreakByWordWrapping;

        NSDictionary *attributes = @{
            NSFontAttributeName            : [UIFont systemFontOfSize:12],
            NSParagraphStyleAttributeName  : textStyle,
            NSForegroundColorAttributeName : [UIPrintPageRenderer colorFromHexString:@"#575759"],
            NSBackgroundColorAttributeName : [UIColor clearColor]
        };

        if (topLeft) {
            CGPoint drawPoint = CGPointMake(10, 10);
            [topLeft drawAtPoint:drawPoint withAttributes:attributes];
        }
        if (topRight) {
            CGSize size = [topRight sizeWithAttributes:attributes];
            CGPoint drawPoint = CGPointMake(CGRectGetMaxX(headerRect) - size.width - 10, 10);
            [topRight drawAtPoint:drawPoint withAttributes:attributes];
        }
        textStyle = nil;
        attributes = nil;
    }
}

- (void)drawFooterForPageAtIndex:(NSInteger)pageIndex
                          inRect:(CGRect)footerRect {

    @autoreleasepool {
        NSMutableParagraphStyle *textStyle = [[NSMutableParagraphStyle defaultParagraphStyle] mutableCopy];
        textStyle.lineBreakMode = NSLineBreakByWordWrapping;

        NSDictionary *attributes = @{
            NSFontAttributeName            : [UIFont systemFontOfSize:12],
            NSParagraphStyleAttributeName  : textStyle,
            NSForegroundColorAttributeName : [UIPrintPageRenderer colorFromHexString:@"#575759"],
            NSBackgroundColorAttributeName : [UIColor clearColor]
        };

        if (bottomLeft) {
            CGSize size = [bottomRight sizeWithAttributes:attributes];
            CGFloat drawY = CGRectGetMaxY(footerRect) - size.height - 10;
            CGPoint drawPoint = CGPointMake(10, drawY);
            [bottomLeft drawAtPoint:drawPoint withAttributes:attributes];
        }
        if (bottomRight) {
            CGSize size = [bottomRight sizeWithAttributes:attributes];
            CGFloat drawX = CGRectGetMaxX(footerRect) - size.width - 10;
            CGFloat drawY = CGRectGetMaxY(footerRect) - size.height - 10;
            CGPoint drawPoint = CGPointMake(drawX, drawY);
            [bottomRight drawAtPoint:drawPoint withAttributes:attributes];
        }

        textStyle = nil;
        attributes = nil;
    }
}

// Assumes input like "#00FF00" (#RRGGBB).
+ (UIColor *)colorFromHexString:(NSString *)hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hexString];
    [scanner setScanLocation:1]; // bypass '#' character
    [scanner scanHexInt:&rgbValue];
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}

@end
