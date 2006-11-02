/**
 * Copyright (c) 2005-2006, Timothy Stone
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "Timothy Stone" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.petmystone.blojsom.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.util.BlojsomUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * File Gallery Plugin
 * A simple file based photo gallery plugin
 *
 * @author Timothy Stone
 * @version 1.0
 */
public class SimplePhotoGalleryPlugin implements BlojsomPlugin {

    private Log _logger = LogFactory.getLog(SimplePhotoGalleryPlugin.class);
    private ServletContext _servletContext;
    
    /**
     * Default constructor.
     */
    public SimplePhotoGalleryPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link BlojsomConfiguration} information
     * @throws BlojsomPluginException If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        this._servletContext = servletConfig.getServletContext();       
    }

    /**
     * Process the blog entries
     * 
     * @param request  Request
     * @param response Response
     * @param user                {@link BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws BlojsomPluginException If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest request,
                               HttpServletResponse response,
                               BlogUser user,
                               Map context,
                               BlogEntry[] entries) throws BlojsomPluginException {

        ArrayList photos = null;

        String photoAlbum = "";

        String photoAlbumThumbnail = null;

        String photoAlbumList = null;

        String photoAlbumBase = user.getBlog().BLOJSOM_DEFAULT_RESOURCE_DIRECTORY + 
                File.separator + user.getId();
        _logger.debug("photoAlbumBase is: " + photoAlbumBase );
        
        boolean hasInitialSlash = false;
        
        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];
            
            photos = new ArrayList();
            
            if( BlojsomUtils.checkMapForKey( entry.getMetaData(), "photo-album") ) {
                photoAlbum = ((String)entry.getMetaData().get("photo-album")).trim();
                _logger.debug( "photoAlbum is: " + photoAlbum );
                hasInitialSlash = ( photoAlbum.indexOf( "/" ) == 0 );
            } else {
                photoAlbum = "";
            }

            if( BlojsomUtils.checkMapForKey( entry.getMetaData(), "photo-album-list") ) {
                photoAlbumList = ((String)entry.getMetaData().get("photo-album-list")).trim();
                _logger.debug( "photoAlbumList is: " + photoAlbumList );
            } else {
                photoAlbumList = null;
            }

            if( BlojsomUtils.checkMapForKey( entry.getMetaData(), "photo-album-thumbnail") ) {
                photoAlbumThumbnail = ((String)entry.getMetaData().get("photo-album-thumbnail")).trim();
                _logger.debug( "photoAlbumThumbnail is: " + photoAlbumThumbnail );
            } else {
                photoAlbumThumbnail = null;
            }            

            String realPath = _servletContext.getRealPath( photoAlbumBase + 
                    ((hasInitialSlash)? "" : File.separator) + photoAlbum );
            _logger.debug( "photoAlbum real path is: " + realPath );

            if( photoAlbum != null && photoAlbum.trim().length() > 0 ) {
                try {
                    
                    File path = new File( realPath );
                    
                    if( path.isDirectory() ) {
                        
                        if( photoAlbumList != null ) {
                            String[] filesInList = 
                                    ((String) entry.getMetaData().get("photo-album-list")).split(",");
                            for( int file = 0; file < filesInList.length; file++ ) {
                                if( (filesInList[file].toLowerCase().indexOf("thumb")) > 0 ) {
                                    continue;
                                }
                                photos.add(photoAlbumBase + File.separator + 
                                        photoAlbum + File.separator + filesInList[file]);
                            }

                        } else {
                            
                            File[] files = path.listFiles( BlojsomUtils.getExtensionsFilter( new String[] {".jpg",".gif",".png"} ) );
                            for( int file = 0; file < files.length; file++ ) {
                                if( (files[file].getName()).toLowerCase().indexOf("thumb") > 0 ) {
                                    continue;
                                }
                                photos.add(photoAlbumBase + ((hasInitialSlash)? "" : File.separator) + 
                                        photoAlbum + File.separator + files[file].getName());
                            }
                        }
                        photoAlbumThumbnail = photoAlbumBase + ((hasInitialSlash)? "" : File.separator) + 
                                photoAlbum + File.separator + photoAlbumThumbnail;
                   }
                    
                    
                    if( path.isFile() ) {
                        photos.add( photoAlbumBase + ((hasInitialSlash)? "" : File.separator) + photoAlbum );
                        photoAlbumThumbnail = photoAlbumBase + ((hasInitialSlash)? "" : File.separator) + 
                                photoAlbumThumbnail;
                    }
                    
                    if( photoAlbumThumbnail != null ) {
                        entry.getMetaData().put( "photo-album-thumbnail", photoAlbumThumbnail );
                        _logger.debug( "photoAlbumThumbnail is: " + photoAlbumThumbnail );
                    }
                    
                    entry.getMetaData().put( "photo-album", photos );
                    
                    _logger.debug( "Photo album loaded with " + 
                            photos.size() + ((photos.size() == 1)? " photo.":" photos.") );

                } catch( NullPointerException npe ) {
                    _logger.error( "Error while trying to retrieve photo album");
                }
                
            } else {
                _logger.debug( "No photo album defined. Moving on...");
            }
        }
        
        return entries;
    }
    

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws BlojsomPluginException If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws BlojsomPluginException If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }
    
}


