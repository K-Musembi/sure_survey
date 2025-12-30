import { useState, useEffect } from 'react'
import { Card, Button, Modal, ModalHeader, ModalBody, ModalContext, ModalFooter, modalTheme, Label, TextInput, FileInput, HelperText, Badge, Alert, Select, Spinner } from 'flowbite-react'
import { distributionAPI, surveyAPI } from '../services/apiServices'
import { HiUserGroup, HiUpload, HiPlus, HiEye, HiExclamationCircle, HiPaperAirplane } from 'react-icons/hi'

const SurveyContacts = ({ surveyId }) => {
  const [lists, setLists] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [showUploadModal, setShowUploadModal] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  
  // Upload State
  const [uploadFile, setUploadFile] = useState(null)
  const [uploadName, setUploadName] = useState('')
  const [isUploading, setIsUploading] = useState(false)
  
  // Sending State
  const [selectedListId, setSelectedListId] = useState('')
  const [isSending, setIsSending] = useState(false)

  const fetchLists = async () => {
    setIsLoading(true)
    setError('')
    try {
      const response = await distributionAPI.getLists()
      setLists(Array.isArray(response.data) ? response.data : [])
    } catch (error) {
      console.error('Failed to fetch lists', error)
      setError('Failed to load contact lists.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchLists()
  }, [])

  const validateCsvFile = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        const text = e.target.result;
        const lines = text.split('\n').filter(line => line.trim() !== '');
        
        if (lines.length === 0) {
          reject('The CSV file is empty.');
          return;
        }

        const header = lines[0].split(',').map(h => h.toLowerCase().trim());
        let phoneIndex = -1;

        // Try to find phone column by header
        for (let i = 0; i < header.length; i++) {
           if (header[i].includes('phone') || header[i].includes('mobile') || header[i].includes('number') || header[i].includes('tel')) {
               phoneIndex = i;
               break;
           }
        }

        // Fallback to first column if no header match found (and assuming first row is header, so check data from row 1)
        if (phoneIndex === -1) {
            phoneIndex = 0;
        }

        // Check first few data rows
        for (let i = 1; i < Math.min(lines.length, 6); i++) {
            const columns = lines[i].split(',');
            if (columns.length > phoneIndex) {
                const phone = columns[phoneIndex].trim();
                // Basic check: if it has letters, it's likely invalid
                if (/[a-zA-Z]/.test(phone) && phone.length > 2) { 
                    reject(`Row ${i+1}: The column '${header[phoneIndex] || 'Column ' + (phoneIndex+1)}' contains '${phone}', which does not appear to be a valid phone number.`);
                    return;
                }
            }
        }
        resolve(true);
      };
      reader.onerror = () => reject('Failed to read file.');
      reader.readAsText(file);
    });
  }

  const handleUpload = async (e) => {
    e.preventDefault()
    if (!uploadFile || !uploadName) return

    setShowUploadModal(false) // Close modal immediately as requested
    setIsUploading(true)
    setError('')
    
    try {
      // Client-side validation
      await validateCsvFile(uploadFile);

      await distributionAPI.uploadCsv(uploadFile, uploadName)
      setUploadFile(null)
      setUploadName('')
      fetchLists()
      setSuccess('List uploaded successfully!')
    } catch (error) {
      console.error('Upload failed', error)
      setError('Upload failed: ' + (error.response?.data?.message || error.message || error))
    } finally {
      setIsUploading(false)
    }
  }

  const handleSendToDistList = async () => {
    if (!surveyId || !selectedListId) return
    setIsSending(true)
    setError('')
    setSuccess('')
    try {
      await surveyAPI.sendToDistributionList(surveyId, selectedListId)
      setSuccess('Survey distribution triggered!')
    } catch (error) {
      console.error('Distribution failed', error)
      setError('Failed to send: ' + (error.response?.data?.message || error.message))
    } finally {
      setIsSending(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
           <h3 className="text-xl font-bold text-gray-900">Distribution Lists</h3>
           <p className="text-gray-600 text-sm">Manage and select contacts for this survey.</p>
        </div>
        <div className="flex gap-2">
           <Button size="sm" onClick={() => setShowUploadModal(true)}>
             <HiUpload className="mr-2 h-4 w-4" />
             Upload CSV
           </Button>
        </div>
      </div>

      {isUploading && (
        <Alert color="info" icon={Spinner}>
          <span className="ml-2 font-medium">Processing CSV upload... Please wait.</span>
        </Alert>
      )}

      {error && (
        <Alert color="failure" icon={HiExclamationCircle} onDismiss={() => setError('')}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert color="success" icon={HiExclamationCircle} onDismiss={() => setSuccess('')}>
          {success}
        </Alert>
      )}
      
      {/* Send Section */}
      <Card className="bg-gray-50 border-gray-200">
        <div className="flex flex-col sm:flex-row gap-4 items-end">
          <div className="w-full">
            <Label htmlFor="sendList" value="Select List to Distribute To" />
            <Select 
              id="sendList"
              value={selectedListId}
              onChange={(e) => setSelectedListId(e.target.value)}
            >
              <option value="">-- Select List --</option>
              {lists.map(l => (
                <option key={l.id} value={l.id}>{l.name} ({l.contacts?.length || 0} contacts)</option>
              ))}
            </Select>
          </div>
          <Button 
            onClick={handleSendToDistList} 
            disabled={!selectedListId || isSending}
            gradientDuoTone="purpleToBlue"
          >
            {isSending ? <Spinner size="sm" className="mr-2" /> : <HiPaperAirplane className="mr-2 h-5 w-5" />}
            {isSending ? 'Sending...' : 'Send Survey'}
          </Button>
        </div>
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {lists.map((list) => (
          <Card key={list.id} className={selectedListId === list.id ? "ring-2 ring-primary-500" : ""}>
             <div className="flex justify-between items-start">
               <div className="flex items-center">
                 <div className="p-2 bg-purple-100 rounded-lg mr-3">
                   <HiUserGroup className="w-5 h-5 text-purple-600" />
                 </div>
                 <div>
                   <h5 className="font-bold text-gray-900">{list.name}</h5>
                   <p className="text-xs text-gray-500">
                     {new Date(list.createdAt || Date.now()).toLocaleDateString()}
                   </p>
                 </div>
               </div>
               <Badge color="gray">{list.contacts?.length || 0}</Badge>
             </div>
             
             <div className="mt-4 flex justify-between items-center">
               <Button size="xs" color="gray">
                 <HiEye className="mr-1 h-3 w-3" /> View
               </Button>
               {selectedListId !== list.id && (
                 <Button size="xs" color="light" onClick={() => setSelectedListId(list.id)}>
                   Select
                 </Button>
               )}
             </div>
          </Card>
        ))}
      </div>

      {/* Upload Modal */}
      <Modal show={showUploadModal} onClose={() => setShowUploadModal(false)}>
        <ModalHeader>Upload CSV Contacts</ModalHeader>
        <ModalBody>
           <form onSubmit={handleUpload} className="space-y-4">
             <div>
               <Label htmlFor="listName" value="Contact List Name" />
               <TextInput 
                 id="listName" 
                 placeholder="e.g. Q1 Customers" 
                 required 
                 value={uploadName}
                 onChange={(e) => setUploadName(e.target.value)}
               />
             </div>
             
             <div>
               <Label htmlFor="dropzone-file" className="flex flex-col items-center justify-center w-full h-40 border-2 border-gray-300 border-dashed rounded-lg cursor-pointer bg-gray-50 hover:bg-gray-100 dark:border-gray-600 dark:bg-gray-700 dark:hover:border-gray-500 dark:hover:bg-gray-600">
                 <div className="flex flex-col items-center justify-center pt-5 pb-6">
                   <HiUpload className="w-10 h-10 mb-3 text-gray-400" />
                   <p className="mb-2 text-sm text-gray-500 dark:text-gray-400">
                     <span className="font-semibold">{uploadFile ? uploadFile.name : 'Click to upload'}</span> or drag and drop
                   </p>
                   <p className="text-xs text-gray-500 dark:text-gray-400">CSV files only</p>
                 </div>
                 <input 
                   id="dropzone-file" 
                   type="file" 
                   className="hidden" 
                   accept=".csv"
                   required
                   onChange={(e) => setUploadFile(e.target.files[0])}
                 />
               </Label>
               
               <HelperText className="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded-md border border-gray-200 block">
                 <span className="font-semibold block mb-1 text-purple-700">Recommended CSV Format:</span>
                 <span className="block mb-2">Include a header row with columns like <strong>Phone</strong>, <strong>Name</strong>, <strong>Email</strong>.</span>
                 <span className="font-mono text-xs text-gray-500">Default (if no headers): Phone, First Name, Last Name, Email</span>
              </HelperText>
             </div>
             
             <div className="flex justify-end pt-2">
               <Button type="submit" disabled={isUploading || !uploadFile}>
                 Upload & Process List
               </Button>
             </div>
           </form>
        </ModalBody>
      </Modal>
    </div>
  )
}

export default SurveyContacts