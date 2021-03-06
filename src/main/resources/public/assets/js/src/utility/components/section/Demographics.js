var React = require('react');
var ReactDOM = require('react-dom');
var Bootstrap = require('react-bootstrap');
var { Link } = require('react-router');
var Breadcrumb = require('../Breadcrumb');
var Table = require('../Table');
var Chart = require('../Chart');
var UpsertFavouriteForm = require('./demographics/UpsertFavouriteForm');
var CreateGroupForm = require('./demographics/CreateGroupForm');
var MessageAlert = require('../AlertDismissable');
var Modal = require('../Modal');

var errorsCodes = require('../../constants/Errors');
var successCodes = require('../../constants/Successes');

var Helpers = require('../../helpers/helpers');

var DemographicsActions = require('../../actions/DemographicsActions');
var DemographicsTablesSchema = require('../../constants/DemographicsTablesSchema');

var { connect } = require('react-redux');
var { bindActionCreators } = require('redux');

var Demographics = React.createClass({
	contextTypes: {
	    intl: React.PropTypes.object
	},
	
	getInitialState() {
		return {
			key: 1,
			showAddNewUserForm: false
    	};
	},
	
	componentWillMount : function() {
	  this.props.getGroupsAndFavourites();
	  //this.props.getGroupMembers('c1403676-de58-4960-bd0a-5d406f533807');	  
  },
  
  componentWillUnmount : function() {
    this.props.resetDemograhpics();
  },

	selectSection(key) {
		this.setState({key : key});
  	},
  	
  	showAddNewUserForm: function (){
  		this.setState({showAddNewUserForm : true});
  	},
  	
  	setGroupsFilter : function(){
  	  this.props.setGroupsFilter(this.refs.searchGroups.getValue());
  	},
  	
  	clearGroupsFilter : function(){
  	  this.props.setGroupsFilter('');
    },
    
    setFavouritesFilter : function(){
      this.props.setFavouritesFilter(this.refs.searchFavourites.getValue());
    },
    
    clearFavouritesFilter : function(){
      this.props.setFavouritesFilter('');
    },
  	
  	computeVisibleItems : function(items, filter){
  	  var visibleItems = {
  	      fields : items.fields,
  	      rows : [],
  	      pager : {
  	        index : items.pager.index,
  	        size : items.pager.size,
  	        count : 1
  	      }
  	  };
  	  
  	  if (filter){
  	    visibleItems.rows = Helpers.pickQualiffiedOnSusbstring(items.rows, 'name', filter, false);
  	  } else {
  	    visibleItems.rows = items.rows;
  	  }
  	  visibleItems.pager.count = Math.ceil(visibleItems.rows.length / visibleItems.pager.size);
  	  return visibleItems;
  	},
  	
  	deleteGroup: function (){
  	  this.props.hideModal();
  	  this.props.deleteGroup(this.props.ModalForm.itemId);
  	},
  	
  	deleteFavourite: function (){
      this.props.hideModal();
      this.props.deleteFavourite(this.props.ModalForm.itemId);
    },
  	
  	showDeleteGroupModal: function(group_id){
  	  
  	  var _t = this.context.intl.formatMessage;
  	  var title = _t({id:'Modal.DeleteGroup.Title'});
  	  
  	  var group = Helpers.pickQualiffiedOnEquality(this.props.groups.rows, 'id', group_id);
  	    	  
  	  var body;
  	  if (group){
  	    body = _t({id:'Modal.DeleteGroup.Body.Part1'}) + group[0].name + _t({id:'Modal.DeleteGroup.Body.Part2'});
      } else {
        body = _t({id:'Modal.DeleteGroup.Body.Part1'}) + group_id + _t({id:'Modal.DeleteGroup.Body.Part2'});
      }  	  
  	  
  	  
  	  var actions = [{
        action: this.props.hideModal,
        name: _t({id:'Buttons.Cancel'})
        }, {
          action: this.deleteGroup,
          name: _t({id:'Buttons.DeleteGroup'}),
          style: 'danger'
        }
      ];
  	  
  	  this.props.showModal(group_id, title, body, actions);
  	},
  	
    showDeleteFavouriteModal: function(favourite_id){
      
      var _t = this.context.intl.formatMessage;
      var title = _t({id:'Modal.DeleteFavourite.Title'});
            
      var favourite = Helpers.pickQualiffiedOnEquality(this.props.favourites.rows, 'id', favourite_id);
            
      var body;
      if (favourite){
        body = _t({id:'Modal.DeleteFavourite.Body.Part1'}) + favourite[0].name + _t({id:'Modal.DeleteFavourite.Body.Part2'});
      } else {
        body = _t({id:'Modal.DeleteFavourite.Body.Part1'}) + favourite_id + _t({id:'Modal.DeleteFavourite.Body.Part2'});
      }     
      
      var actions = [{
        action: this.props.hideModal,
        name: _t({id:'Buttons.Cancel'})
        }, {
          action: this.deleteFavourite,
          name: _t({id:'Buttons.DeleteFavourite'}),
          style: 'danger'
        }
      ];
      
      this.props.showModal(favourite_id, title, body, actions);
    },
  	
	
  	render: function() {
  	  var _t = this.context.intl.formatMessage;
  		var self = this;
  		
  		var visibleGroups = this.computeVisibleItems(this.props.groups, this.props.groupsFilter);
  		visibleGroups.fields.forEach(function(field){
  		  if(field.hasOwnProperty('name') && field.name === 'add-favourite'){
  		    field.handler = function (){
  		      self.props.showFavouriteGroupForm(this.props.row.id);
  		    };
  		  } else if (field.hasOwnProperty('name') && field.name === 'remove'){
          field.handler = function (){
            self.showDeleteGroupModal(this.props.row.id);
          };
        }
  		});
  		
  		var visibleFavourites = this.computeVisibleItems(this.props.favourites, this.props.favouritesFilter);
  		visibleFavourites.fields.forEach(function(field){
        if(field.hasOwnProperty('name') && field.name === 'remove'){
          field.handler = function (){
            self.showDeleteFavouriteModal(this.props.row.id);
          };
        }
      });
  		
  		var chartData = {
		    series: [{
		        legend: 'Alicante DAIAD Trial (average)',
		        xAxis: 'date',
		        yAxis: 'volume',
		        data: [{
		            id: 1,
		            volume: 25,
		            date: new Date(2016, 1, 1)
		        }, {
		            id: 1,
		            volume: 70,
		            date: new Date(2016, 1, 2) 
		        }, {
		            id: 1,
		            volume: 75,
		            date: new Date(2016, 1, 3)
		        }, {
		            id: 1,
		            volume: 62,
		            date: new Date(2016, 1, 4)
		        }, {
		            id: 1,
		            volume: 53,
		            date: new Date(2016, 1, 5)
		        }, {
		            id: 1,
		            volume: 27,
		            date: new Date(2016, 1, 6)
		        }, {
		            id: 1,
		            volume: 41,
		            date: new Date(2016, 1, 7)
		        }, {
		            id: 1,
		            volume: 45,
		            date: new Date(2016, 1, 8)
		        }, {
		            id: 1,
		            volume: 13,
		            date: new Date(2016, 1, 9)
		        }]
		    }, {
		        legend: 'User 1',
		        xAxis: 'date',
		        yAxis: 'volume',
		        data: [{
		            id: 1,
		            volume: 15,
		            date: new Date(2016, 1, 1)
		        }, {
		            id: 1,
		            volume: 30,
		            date: new Date(2016, 1, 2) 
		        }, {
		            id: 1,
		            volume: 44,
		            date: new Date(2016, 1, 3)
		        }, {
		            id: 1,
		            volume: 32,
		            date: new Date(2016, 1, 4)
		        }, {
		            id: 1,
		            volume: 23,
		            date: new Date(2016, 1, 5)
		        }, {
		            id: 1,
		            volume: 11,
		            date: new Date(2016, 1, 6)
		        }, {
		            id: 1,
		            volume: 18,
		            date: new Date(2016, 1, 7)
		        }, {
		            id: 1,
		            volume: 11,
		            date: new Date(2016, 1, 8)
		        }, {
		            id: 1,
		            volume: 5,
		            date: new Date(2016, 1, 9)
		        }]
		      }]
		  };

  		var jobs = {
  			fields: [{
  				name: 'id',
  				hidden: true
  			}, {
  				name: 'description',
  				title: 'Demographics.JobManagement.Description'
  			}, {
  				name: 'owner',
  				title: 'Demographics.JobManagement.Owner'			
  			}, {
  				name: 'createdOn',
  				title: 'Demographics.JobManagement.CreatedOn',
  				type: 'datetime'
  			}, {
  				name: 'scheduledOn',
  				title: 'Demographics.JobManagement.NextExecution',
  				type: 'datetime'
  			}, {
  				name: 'status',
  				title: 'Demographics.JobManagement.Status'
  			}, {
  				name: 'progress',
  				title: 'Demographics.JobManagement.Progress',
  				type: 'progress'
  			}, {
  				name: 'edit',
  				type:'action',
  				icon: 'pencil',
  				handler: function() {
  					console.log(this);
  				}
  			}, {
  				name: 'cancel',
  				type:'action',
  				icon: 'remove',
  				handler: function() {
  					console.log(this);
  				}
  			}],
  			rows: [{
  				id: 1,
  				description: 'Find top 20 consumers for January 2016',
  				owner: 'Yannis',
  				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  				scheduledOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  				status: 'Running',
  				progress: 45
  			}, {
  				id: 2,
  				description: 'Create clusters of users based on consumption behavior patterns',
  				owner: 'Yannis',
  				createdOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  				scheduledOn: new Date((new Date()).getTime() + Math.random() * 3600000),
  				status: 'Pending',
  				progress: null
  			}],
  			pager: {
  				index: 0,
  				size: 1,
  				count:2
  			}
  		};
  		
  		
      var chartOptions = {
          tooltip: {
              show: true
          }
      };
      
      const breadCrumb = (
            <div className="row">
              <div className="col-md-12">
                <Breadcrumb routes={this.props.routes}/>
              </div>
            </div>
      );
      
  		const groupTitle = (
  			<span>
  				<i className='fa fa-group fa-fw'></i>
  				<span style={{ paddingLeft: 4 }}>Groups</span>
  				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
  					<Bootstrap.Button	bsStyle="default" className="btn-circle" onClick={this.props.showNewGroupForm}>
  						<Bootstrap.Glyphicon glyph="plus" />
  					</Bootstrap.Button>
  				</span>
  			</span>
  		);
  		
  		const favouriteTitle = (
  			<span>
  				<i className='fa fa-bookmark fa-fw'></i>
  				<span style={{ paddingLeft: 4 }}>Favourites</span>
  			</span>
  		);
  		
  		const newGroupInfoFormTitle = (
          <span>
            <i className='fa fa-group fa-fw'></i>
            <span style={{ paddingLeft: 4 }}>{_t({ id:'Demographics.NewGroup.NewGroup'})}</span>
          </span>
        );
  		
  		const membersTitle = (
          <span>
            <i className='fa fa-user fa-fw'></i>
            <span style={{ paddingLeft: 4 }}>{_t({ id:'Demographics.NewGroup.CurrentMembers'})}</span>
          </span>
        );
  		
  		const nonMembersTitle = (
          <span>
            <i className='fa fa-user-plus fa-fw'></i>
            <span style={{ paddingLeft: 4 }}>{_t({ id:'Demographics.NewGroup.PossibleMembers'})}</span>
          </span>
        );
  
  		const chartTitle = (
  			<span>
  				<i className="fa fa-bar-chart fa-fw"></i>
  				<span style={{ paddingLeft: 4 }}>Compare groups and users</span>
  			</span>
  		);	

  		const scheduleTitle = (
  			<span>
  				<i className='fa fa-clock-o fa-fw'></i>
  				<span style={{ paddingLeft: 4 }}>Job Management</span>
  				<span style={{float: 'right',  marginTop: -3, marginLeft: 5 }}>
  					<Bootstrap.Button	bsStyle="default" className="btn-circle">
  						<Bootstrap.Glyphicon glyph="plus" />
  					</Bootstrap.Button>
  				</span>
  			</span>
  		);	
  		
  		const groupMessageAlert = (
          <MessageAlert
            show = {this.props.showMessageAlert}
            title = {this.props.asyncResponse.success ?  _t({id: 'Form.Success'}) : _t({id: 'Form.ErrorsDetected'})}
            i18nNamespace={this.props.asyncResponse.success ? 'Success.' : 'Error.'}
            bsStyle={this.props.asyncResponse.success ? 'success' : 'danger'}
            format='list'
            messages={!this.props.asyncResponse.success ? this.props.asyncResponse.errors : [{code: successCodes['GroupSuccess.GROUP_DELETED']}]}
            dismissFunc={this.props.hideMessageAlert}
          />
        );
  		
  		var modal;
  		if (this.props.hasOwnProperty('ModalForm') && 
  		    this.props.ModalForm.hasOwnProperty('modal') &&
  		    this.props.ModalForm.modal.hasOwnProperty('show') &&
  		    this.props.ModalForm.modal.show){

  		  modal = (
  		      <Modal show = {true}
              onClose = {self.props.hideModal}
              title = {this.props.ModalForm.modal.title}
              text = {this.props.ModalForm.modal.body}
  		        actions = {this.props.ModalForm.modal.actions}
  		      />
  		      );
  		} else {
  		  modal = (
            <Modal show = {false}
              onClose = {function(){}}
              title = {''}
              text = {''}
              actions = {[]}
            />
            );
  		}
  		
  		if (this.props.application === 'addNewGroup'){
  		  return (
		      <div className="container-fluid" style={{ paddingTop: 10 }}>
            {breadCrumb}
            <CreateGroupForm
              
              newGroupName = {this.props.newGroup.newGroupName}
              currentMembers = {this.props.newGroup.currentMembers}
              possibleMembers = {this.props.newGroup.possibleMembers}
            
              messageAlert = {{
                show: this.props.newGroup.showMessageAlert,
                success: this.props.asyncResponse.success,
                errors: this.props.asyncResponse.errors,
                dismissFunc : this.props.addGroupHideErrorAlert
              }}
              actions = {{
                toggleCandidateGroupMemberToRemove: this.props.toggleCandidateGroupMemberToRemove,
                toggleCandidateGroupMemberToAdd: this.props.toggleCandidateGroupMemberToAdd,
                addSelectedGroupMembers: this.props.addSelectedGroupMembers,
                removeSelectedGroupMembers: this.props.removeSelectedGroupMembers,
                hideNewGroupForm: this.props.hideNewGroupForm,
                getGroupsAndFavourites: this.props.getGroupsAndFavourites,
                setGroupName: this.props.setGroupName,
                createGroupSet: this.props.createGroupSet,
                addGroupValidationErrorsOccurred: this.props.addGroupValidationErrorsOccurred
              }}
            />
          </div>
  		  );
  		} else if (this.props.application === 'favouriteGroupForm' && this.props.favouriteGroupId){
        return (
            <div className="container-fluid" style={{ paddingTop: 10 }}>
              {breadCrumb}
              <UpsertFavouriteForm
                type = 'GROUP'
                itemId = {this.props.favouriteGroupId}
                actions = {{
                  cancelAction : this.props.hideFavouriteGroupForm,
                  refreshParentForm : this.props.getGroupsAndFavourites
                }}
                
              />
            </div>
        );
      } else {
    		if (!this.props.isLoading){
    		  return (
      			<div className="container-fluid" style={{ paddingTop: 10 }}>
      			  {breadCrumb}
      			  {groupMessageAlert}
      			  <div className='row'>
                <div className='col-md-12'>           
                  {modal}
                </div>
              </div>
      				<div className='row'>
      					<div className='col-md-6'>
      					 	<Bootstrap.Input 	type="text" 
      					 	          ref="searchGroups"
      					 						placeholder='Search groups ...'
      					 						onChange={this.setGroupsFilter}
      					 						buttonAfter={<Bootstrap.Button onClick={this.clearGroupsFilter}><i className='fa fa-trash fa-fw'></i></Bootstrap.Button>} 
      					 	/>
      			 		</div>
      					<div className='col-md-6'>
      					 	<Bootstrap.Input 	type="text" 
      					 	          ref="searchFavourites"
      					 						placeholder='Search favourites ...' 
      					 						onChange={this.setFavouritesFilter}
      					 						buttonAfter={<Bootstrap.Button onClick={this.clearFavouritesFilter}><i className='fa fa-trash fa-fw'></i></Bootstrap.Button>} 
      					 	/>
      			 		</div>
      			 	</div>
      				<div className='row'>
      					<div className='col-md-6'>
      						<Bootstrap.Panel header={groupTitle}>
      							<Bootstrap.ListGroup fill>
      								<Bootstrap.ListGroupItem>
      									<Table data={visibleGroups}></Table>
      								</Bootstrap.ListGroupItem>
      							</Bootstrap.ListGroup>
      						</Bootstrap.Panel>
      					</div>
      					<div className='col-md-6'>
      						<Bootstrap.Panel header={favouriteTitle}>
      							<Bootstrap.ListGroup fill>
      								<Bootstrap.ListGroupItem>	
      									<Table data={visibleFavourites}></Table>
      								</Bootstrap.ListGroupItem>
      							</Bootstrap.ListGroup>
      						</Bootstrap.Panel>
      					</div>
      				</div>
      				<div className="row">
      					<div className="col-md-12">
      						<Bootstrap.Panel header={chartTitle}>
      							<Bootstrap.ListGroup fill>
      								<Bootstrap.ListGroupItem>
      									<Chart 	style={{ width: '100%', height: 400 }} 
      											elementClassName='mixin'
      											prefix='chart'
      											options={chartOptions}
      											data={chartData}/>
      								</Bootstrap.ListGroupItem>
      								<Bootstrap.ListGroupItem>
      									<span style={{ paddingLeft : 7}}> </span>
      									<Link to='/analytics' style={{ paddingLeft : 7, float: 'right'}}>View analytics</Link>
      								</Bootstrap.ListGroupItem>
      							</Bootstrap.ListGroup>
      						</Bootstrap.Panel>
      					</div>
      				</div>
      				<div className="row">
      					<div className='col-md-12'>
      						<Bootstrap.Panel header={scheduleTitle}>
      							<Bootstrap.ListGroup fill>
      								<Bootstrap.ListGroupItem>	
      									<Table data={jobs}></Table>
      								</Bootstrap.ListGroupItem>
      								<Bootstrap.ListGroupItem>
      									<span style={{ paddingLeft : 7}}> </span>
      									<Link to='/scheduler' style={{ paddingLeft : 7, float: 'right'}}>View job management</Link>
      								</Bootstrap.ListGroupItem>
      							</Bootstrap.ListGroup>
      						</Bootstrap.Panel>
      					</div>
      				</div>
      			</div>
      		);
		    } else {
	        return (
	          <div>
	            <img className='preloader' src='/assets/images/utility/preloader-counterclock.png' />
	            <img className='preloader-inner' src='/assets/images/utility/preloader-clockwise.png' />
	          </div>
	        );
		    }
  		}
  	}
});

Demographics.icon = 'bookmark';
Demographics.title = 'Section.Demographics';


function mapStateToProps(state) {
  return {
    isLoading : state.demographics.isLoading,
    asyncResponse : state.demographics.asyncResponse,
    newGroup : state.demographics.newGroup,
    application : state.demographics.application,
    groupsFilter : state.demographics.groupsFilter,
    groups : state.demographics.groups,
    favouritesFilter : state.demographics.favouritesFilter,
    favourites : state.demographics.favourites,
    favouriteGroupId : state.demographics.favouriteGroupId,
    showMessageAlert : state.demographics.showMessageAlert,
    
    ModalForm : state.demographics.ModalForm
  };
}

function mapDispatchToProps(dispatch) {
  return {
    getGroupsAndFavourites: bindActionCreators(DemographicsActions.getGroupsAndFavourites, dispatch),
    createGroupSet : bindActionCreators(DemographicsActions.createGroupSet, dispatch),
    toggleCandidateGroupMemberToAdd : bindActionCreators(DemographicsActions.toggleCandidateGroupMemberToAdd, dispatch),
    toggleCandidateGroupMemberToRemove : bindActionCreators(DemographicsActions.toggleCandidateGroupMemberToRemove, dispatch),
    addSelectedGroupMembers : bindActionCreators(DemographicsActions.addSelectedGroupMembers, dispatch),
    removeSelectedGroupMembers : bindActionCreators(DemographicsActions.removeSelectedGroupMembers, dispatch),
    getGroupMembers : bindActionCreators(DemographicsActions.getGroupMembers, dispatch),
    setGroupName : bindActionCreators(DemographicsActions.setGroupName, dispatch),
    addGroupValidationErrorsOccurred : bindActionCreators(DemographicsActions.addGroupValidationErrorsOccurred, dispatch),
    addGroupHideErrorAlert :  bindActionCreators(DemographicsActions.addGroupHideErrorAlert, dispatch),
    setGroupsFilter : bindActionCreators(DemographicsActions.setGroupsFilter, dispatch),
    setFavouritesFilter : bindActionCreators(DemographicsActions.setFavouritesFilter, dispatch),
    showNewGroupForm : bindActionCreators(DemographicsActions.showNewGroupForm, dispatch),
    hideNewGroupForm : bindActionCreators(DemographicsActions.hideNewGroupForm, dispatch),
    showFavouriteGroupForm : bindActionCreators(DemographicsActions.showFavouriteGroupForm, dispatch),
    hideFavouriteGroupForm : bindActionCreators(DemographicsActions.hideFavouriteGroupForm, dispatch),
    resetDemograhpics : bindActionCreators(DemographicsActions.resetDemograhpics, dispatch),
    
    deleteGroup : bindActionCreators(DemographicsActions.deleteGroup, dispatch),
    showModal : bindActionCreators(DemographicsActions.showModal, dispatch),
    hideModal : bindActionCreators(DemographicsActions.hideModal, dispatch),
    deleteFavourite : bindActionCreators(DemographicsActions.deleteFavourite, dispatch),
    hideMessageAlert : bindActionCreators(DemographicsActions.hideMessageAlert, dispatch)
  };
}


module.exports = connect(mapStateToProps, mapDispatchToProps)(Demographics);

